package co.edu.uco.ucochallenge.application.user.registerUser.usecase.impl;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.notification.DuplicateRegistrationNotificationService;
import co.edu.uco.ucochallenge.application.notification.RegistrationAttempt;
import co.edu.uco.ucochallenge.application.user.registerUser.usecase.RegisterUserUseCase;
import co.edu.uco.ucochallenge.crosscuting.exception.DomainException;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageCodes;
import co.edu.uco.ucochallenge.domain.user.model.User;
import co.edu.uco.ucochallenge.domain.user.port.out.UserRepository;

@Service
public class RegisterUserUseCaseImpl implements RegisterUserUseCase {


    private final UserRepository repository;
    private final DuplicateRegistrationNotificationService notificationService;
    public RegisterUserUseCaseImpl(final UserRepository repository,
                                   final DuplicateRegistrationNotificationService notificationService) {
        this.repository = repository;
        this.notificationService = notificationService;
    }

    @Override
    public User execute(final User domain) {
        validateUniqueness(domain);
        final User savedUser = repository.save(domain);
        return savedUser;
    }

    private void validateUniqueness(final User domain) {
        if (repository.existsByEmail(domain.email())) {
            notificationService.notifyEmailConflict(RegistrationAttempt.fromUser(domain));
            throw DomainException.buildFromCatalog(
                MessageCodes.Domain.User.EMAIL_ALREADY_REGISTERED_TECHNICAL,
                MessageCodes.Domain.User.EMAIL_ALREADY_REGISTERED_USER
            );
        }

        if (repository.existsByIdTypeAndIdNumber(domain.idType(), domain.idNumber())) {
            throw DomainException.buildFromCatalog(
                MessageCodes.Domain.User.ID_NUMBER_ALREADY_REGISTERED_TECHNICAL,
                MessageCodes.Domain.User.ID_NUMBER_ALREADY_REGISTERED_USER
            );
        }

        if (repository.existsByMobileNumber(domain.mobileNumber())) {
            notificationService.notifyMobileConflict(RegistrationAttempt.fromUser(domain));
            throw DomainException.buildFromCatalog(
                MessageCodes.Domain.User.MOBILE_ALREADY_REGISTERED_TECHNICAL,
                MessageCodes.Domain.User.MOBILE_ALREADY_REGISTERED_USER
            );
        }
    }
}
