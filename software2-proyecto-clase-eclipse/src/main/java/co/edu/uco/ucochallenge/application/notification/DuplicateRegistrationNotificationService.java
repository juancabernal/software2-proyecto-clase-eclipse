package co.edu.uco.ucochallenge.application.notification;

import static co.edu.uco.ucochallenge.domain.notification.model.NotificationMessage.NotificationEvent.DUPLICATE_EMAIL;
import static co.edu.uco.ucochallenge.domain.notification.model.NotificationMessage.NotificationEvent.DUPLICATE_MOBILE;
import static co.edu.uco.ucochallenge.domain.notification.model.NotificationMessage.NotificationEvent.EMAIL_CONFIRMATION;
import static co.edu.uco.ucochallenge.domain.notification.model.NotificationMessage.NotificationEvent.MOBILE_CONFIRMATION;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.parameter.ParameterCodes;
import co.edu.uco.ucochallenge.domain.notification.model.NotificationMessage;
import co.edu.uco.ucochallenge.domain.notification.model.NotificationMessage.NotificationEvent;
import co.edu.uco.ucochallenge.domain.notification.model.NotificationMessage.Person;
import co.edu.uco.ucochallenge.domain.notification.model.NotificationMessage.Recipient;
import co.edu.uco.ucochallenge.domain.notification.port.out.NotificationSenderPort;
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

    private final NotificationSenderPort notificationSenderPort;
    private final NotificationRecipientsProvider recipientsProvider;
    private final ParameterServicePort parameterServicePort;

    public DuplicateRegistrationNotificationService(final NotificationSenderPort notificationSenderPort,
            final NotificationRecipientsProvider recipientsProvider,
            final ParameterServicePort parameterServicePort) {
        this.notificationSenderPort = notificationSenderPort;
        this.recipientsProvider = recipientsProvider;
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

    public void notifyEmailConfirmation(final RegistrationAttempt attempt,
            final String verificationCode,
            final int ttlMinutes,
            final int maxAttempts){
    	sendNotification(EMAIL_CONFIRMATION, attempt, EMAIL_CONFIRMATION_TEMPLATE,
                "Confirma tu correo electrónico", "confirmar_datos", "EMAIL", false,
                verificationCode, ttlMinutes, maxAttempts);
    }

    public void notifyMobileConfirmation(final RegistrationAttempt attempt,
            final String verificationCode,
            final int ttlMinutes,
            final int maxAttempts) {
        sendNotification(MOBILE_CONFIRMATION, attempt, MOBILE_CONFIRMATION_TEMPLATE,
                "Confirma tu número de teléfono", "confirmar_datos", "SMS", false,
                verificationCode, ttlMinutes, maxAttempts);
	}
        

    private void sendNotification(final NotificationEvent event,
            final RegistrationAttempt attempt,
            final NotificationTemplate template,
            final String subject,
            final String notificationType,
            final String forceChannel,
            final boolean includeAdminRecipients,
            final Object... extraTemplateArguments) {
        if (attempt == null) {
            LOGGER.debug("Skipping notification '{}' because registration attempt is not available.", event);
            return;
        }

        try {
            final String displayName = resolveDisplayName(attempt);
            final Person attemptedPerson = new Person(displayName, attempt.email(), attempt.mobileNumber());
            final Object[] templateArguments = mergeTemplateArguments(resolveGreetingName(displayName), extraTemplateArguments);
            final String message = template.resolve(parameterServicePort, templateArguments);
            final List<Recipient> recipients = resolveRecipients(attemptedPerson, includeAdminRecipients);

         // 🔹 Crear un mapa con datos adicionales
            final Map<String, Object> extraData = new HashMap<>();
            if (extraTemplateArguments != null && extraTemplateArguments.length >= 3) {
                extraData.put("code", extraTemplateArguments[0]);
                extraData.put("ttlMinutes", extraTemplateArguments[1]);
                extraData.put("maxAttempts", extraTemplateArguments[2]);
            }

            // 🔹 Crear mensaje con data adicional
            final NotificationMessage notificationMessage = new NotificationMessage(
                    event,
                    subject,
                    message,
                    Instant.now(),
                    Person.empty(),
                    attemptedPerson,
                    recipients,
                    notificationType,
                    forceChannel,
                    extraData // 👈 nuevo parámetro
            );


            notificationSenderPort.send(notificationMessage);
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

        appendConfiguredRecipients(recipients, dedupe, recipientsProvider.getAdminRecipients(), "ADMIN");
        appendConfiguredRecipients(recipients, dedupe, recipientsProvider.getUserRecipients(), "USER");
        return recipients;
    }

    private void appendConfiguredRecipients(final List<Recipient> recipients, final Set<String> dedupe,
            final List<Recipient> configuredRecipients, final String defaultRole) {
        for (final Recipient properties : configuredRecipients) {
            if (properties == null || !properties.hasContactInfo()) {
                continue;
            }
            addRecipient(recipients, dedupe, new Recipient(defaultRole,
                    properties.name(),
                    properties.email(),
                    properties.mobileNumber()));
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
    private Object[] mergeTemplateArguments(final Object first, final Object... others) {
        if (others == null || others.length == 0) {
            return new Object[] { first };
        }
        final Object[] merged = new Object[others.length + 1];
        merged[0] = first;
        System.arraycopy(others, 0, merged, 1, others.length);
        return merged;
    }
}
