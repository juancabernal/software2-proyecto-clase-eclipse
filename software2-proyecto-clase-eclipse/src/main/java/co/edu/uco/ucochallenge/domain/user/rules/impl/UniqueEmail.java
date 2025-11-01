package co.edu.uco.ucochallenge.domain.user.rules.impl;

import org.springframework.stereotype.Component;

import co.edu.uco.ucochallenge.application.notification.DuplicateRegistrationNotificationService;
import co.edu.uco.ucochallenge.application.notification.RegistrationAttempt;
import co.edu.uco.ucochallenge.crosscuting.exception.DomainException;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageCodes;
import co.edu.uco.ucochallenge.domain.user.model.User;
import co.edu.uco.ucochallenge.domain.user.port.out.UserRepository;

@Component
public class UniqueEmail {

    private final UserRepository repository;
    private final DuplicateRegistrationNotificationService notificationService;

    public UniqueEmail(final UserRepository repository,
            final DuplicateRegistrationNotificationService notificationService) {
        this.repository = repository;
        this.notificationService = notificationService;
    }

    public void validate(final User user) {
        if (user == null) {
            return;
        }

        final String email = TextHelper.getDefaultWithTrim(user.email());
        if (TextHelper.isEmpty(email)) {
            return;
        }

        if (!repository.existsByEmail(email)) {
            return;
        }

        notificationService.notifyEmailConflict(RegistrationAttempt.fromUser(user));
        throw DomainException.buildFromCatalog(MessageCodes.Domain.User.EMAIL_ALREADY_REGISTERED_TECHNICAL,
                MessageCodes.Domain.User.EMAIL_ALREADY_REGISTERED_USER);
    }
}