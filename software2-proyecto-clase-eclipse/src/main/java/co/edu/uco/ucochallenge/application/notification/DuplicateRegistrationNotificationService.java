package co.edu.uco.ucochallenge.application.notification;

import static co.edu.uco.ucochallenge.application.notification.DuplicateRegistrationNotificationRequest.NotificationEvent.DUPLICATE_EMAIL;
import static co.edu.uco.ucochallenge.application.notification.DuplicateRegistrationNotificationRequest.NotificationEvent.DUPLICATE_MOBILE;
import static co.edu.uco.ucochallenge.application.notification.DuplicateRegistrationNotificationRequest.NotificationEvent.EMAIL_CONFIRMATION;
import static co.edu.uco.ucochallenge.application.notification.DuplicateRegistrationNotificationRequest.NotificationEvent.MOBILE_CONFIRMATION;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import co.edu.uco.ucochallenge.application.notification.DuplicateRegistrationNotificationRequest.NotificationEvent;
import co.edu.uco.ucochallenge.application.notification.DuplicateRegistrationNotificationRequest.Person;
import co.edu.uco.ucochallenge.application.notification.DuplicateRegistrationNotificationRequest.Recipient;
import co.edu.uco.ucochallenge.application.notification.NotificationApiProperties.RecipientProperties;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.parameter.ParameterCodes;
import co.edu.uco.ucochallenge.infrastructure.secondary.ports.service.ParameterServicePort;

@Component
public class DuplicateRegistrationNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DuplicateRegistrationNotificationService.class);

    private static final NotificationTemplate DUPLICATED_EMAIL_TEMPLATE = new NotificationTemplate(
            ParameterCodes.Notification.DUPLICATED_EMAIL_TEMPLATE,
            "Hola %s, detectamos un intento de registro con su correo electrónico.");
    private static final NotificationTemplate DUPLICATED_MOBILE_TEMPLATE = new NotificationTemplate(
            ParameterCodes.Notification.DUPLICATED_MOBILE_TEMPLATE,
            "Hola %s, detectamos un intento de registro con su número de teléfono móvil.");
    private static final NotificationTemplate EMAIL_CONFIRMATION_TEMPLATE = new NotificationTemplate(
            ParameterCodes.Notification.EMAIL_CONFIRMATION_STRATEGY,
            "Hola %s, confirma tu correo electrónico para finalizar el registro.");
    private static final NotificationTemplate MOBILE_CONFIRMATION_TEMPLATE = new NotificationTemplate(
            ParameterCodes.Notification.MOBILE_CONFIRMATION_STRATEGY,
            "Hola %s, confirma tu número de teléfono para finalizar el registro.");

    private final NotificationClient notificationClient;
    private final NotificationApiProperties apiProperties;
    private final ParameterServicePort parameterServicePort;

    public DuplicateRegistrationNotificationService(final NotificationClient notificationClient,
            final NotificationApiProperties apiProperties,
            final ParameterServicePort parameterServicePort) {
        this.notificationClient = notificationClient;
        this.apiProperties = apiProperties;
        this.parameterServicePort = parameterServicePort;
    }

    public void notifyEmailConflict(final RegistrationAttempt attempt) {
        sendNotification(DUPLICATE_EMAIL, attempt, DUPLICATED_EMAIL_TEMPLATE,
                "Intento de registro con correo existente", "duplicate_alert", "EMAIL", true);
    }

    public void notifyMobileConflict(final RegistrationAttempt attempt) {
        sendNotification(DUPLICATE_MOBILE, attempt, DUPLICATED_MOBILE_TEMPLATE,
                "Intento de registro con móvil existente", "duplicate_alert", "SMS", true);
    }

    public void notifyEmailConfirmation(final RegistrationAttempt attempt) {
        sendNotification(EMAIL_CONFIRMATION, attempt, EMAIL_CONFIRMATION_TEMPLATE,
                "Confirma tu correo electrónico", "confirmar_datos", "EMAIL", false);
    }

    public void notifyMobileConfirmation(final RegistrationAttempt attempt) {
        sendNotification(MOBILE_CONFIRMATION, attempt, MOBILE_CONFIRMATION_TEMPLATE,
                "Confirma tu número de teléfono", "confirmar_datos", "SMS", false);
    }

    private void sendNotification(final NotificationEvent event,
            final RegistrationAttempt attempt,
            final NotificationTemplate template,
            final String subject,
            final String notificationType,
            final String forceChannel,
            final boolean includeAdminRecipients) {
        if (attempt == null) {
            LOGGER.debug("Skipping notification '{}' because registration attempt is not available.", event);
            return;
        }

        try {
            final String displayName = resolveDisplayName(attempt);
            final Person attemptedPerson = new Person(displayName, attempt.email(), attempt.mobileNumber());
            final String message = template.resolve(parameterServicePort, resolveGreetingName(displayName));
            final List<Recipient> recipients = resolveRecipients(attemptedPerson, includeAdminRecipients);

            final DuplicateRegistrationNotificationRequest request = new DuplicateRegistrationNotificationRequest(
                    event,
                    subject,
                    message,
                    Instant.now(),
                    Person.empty(),
                    attemptedPerson,
                    recipients,
                    notificationType,
                    forceChannel);

            notificationClient.sendNotification(request);
        } catch (final Exception exception) {
            LOGGER.error("Unable to send '{}' notification for '{}'.", event, attempt.email(), exception);
        }
    }

    private String resolveDisplayName(final RegistrationAttempt attempt) {
        final String displayName = attempt.displayName();
        if (!TextHelper.isEmpty(displayName)) {
            return displayName;
        }
        final String email = attempt.email();
        if (!TextHelper.isEmpty(email)) {
            return email;
        }
        final String mobileNumber = attempt.mobileNumber();
        if (!TextHelper.isEmpty(mobileNumber)) {
            return mobileNumber;
        }
        return "Usuario";
    }

    private String resolveGreetingName(final String displayName) {
        if (TextHelper.isEmpty(displayName)) {
            return "Usuario";
        }
        final String[] tokens = displayName.split(" ");
        return tokens.length > 0 ? tokens[0] : displayName;
    }

    private List<Recipient> resolveRecipients(final Person attemptedPerson, final boolean includeAdminRecipients) {
        final List<Recipient> recipients = new ArrayList<>();
        final Set<String> dedupe = new HashSet<>();

        addRecipient(recipients, dedupe, new Recipient("USER", attemptedPerson.name(),
                attemptedPerson.email(), attemptedPerson.mobileNumber()));

        if (!includeAdminRecipients) {
            return recipients;
        }

        final String adminEmailParameter = parameterServicePort.getParameter(ParameterCodes.Notification.ADMIN_EMAIL);
        addRecipient(recipients, dedupe, new Recipient("ADMIN", "Administrador", adminEmailParameter, null));

        appendConfiguredRecipients(recipients, dedupe, apiProperties.getAdminRecipients(), "ADMIN");
        appendConfiguredRecipients(recipients, dedupe, apiProperties.getUserRecipients(), "USER");
        return recipients;
    }

    private void appendConfiguredRecipients(final List<Recipient> recipients, final Set<String> dedupe,
            final List<RecipientProperties> configuredRecipients, final String defaultRole) {
        for (final RecipientProperties properties : configuredRecipients) {
            if (properties == null || !properties.hasContactInfo()) {
                continue;
            }
            addRecipient(recipients, dedupe, new Recipient(defaultRole,
                    properties.getName(),
                    properties.getEmail(),
                    properties.getMobileNumber()));
        }
    }

    private void addRecipient(final List<Recipient> recipients,
            final Set<String> dedupe,
            final Recipient recipient) {
        if (recipient == null || !recipient.hasContactInfo()) {
            return;
        }
        final String key = String.format(Locale.ROOT, "%s|%s",
                TextHelper.getDefaultWithTrim(recipient.email()).toLowerCase(Locale.ROOT),
                TextHelper.getDefaultWithTrim(recipient.mobileNumber()));
        if (!dedupe.add(key)) {
            return;
        }
        recipients.add(recipient);
    }
}