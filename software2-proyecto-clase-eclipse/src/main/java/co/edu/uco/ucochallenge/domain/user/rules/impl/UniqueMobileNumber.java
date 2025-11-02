package co.edu.uco.ucochallenge.domain.user.rules.impl;

import org.springframework.stereotype.Component;

import co.edu.uco.ucochallenge.crosscuting.exception.DomainException;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageCodes;
import co.edu.uco.ucochallenge.domain.user.model.User;
import co.edu.uco.ucochallenge.domain.user.port.out.UserRepository;
import co.edu.uco.ucochallenge.application.notification.DuplicateRegistrationNotificationService;
import co.edu.uco.ucochallenge.application.notification.RegistrationAttempt;

@Component
public class UniqueMobileNumber {

    private final UserRepository repository;
    private final DuplicateRegistrationNotificationService notificationService;

    public UniqueMobileNumber(final UserRepository repository,
            final DuplicateRegistrationNotificationService notificationService) {
        this.repository = repository;
        this.notificationService = notificationService;
    }

    public void validate(final User user) {
        if (user == null) {
            return;
        }

        final String mobileNumber = TextHelper.getDefaultWithTrim(user.mobileNumber());
        if (TextHelper.isEmpty(mobileNumber)) {
            return;
        }

        if (!repository.existsByMobileNumber(mobileNumber)) {
            return;
        }

        notificationService.notifyMobileConflict(RegistrationAttempt.fromUser(user));
        throw DomainException.buildFromCatalog(MessageCodes.Domain.User.MOBILE_ALREADY_REGISTERED_TECHNICAL,
                MessageCodes.Domain.User.MOBILE_ALREADY_REGISTERED_USER);
    }
}