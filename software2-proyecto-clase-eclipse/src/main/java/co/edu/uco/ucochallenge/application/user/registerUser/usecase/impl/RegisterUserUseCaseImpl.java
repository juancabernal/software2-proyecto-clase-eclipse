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
<<<<<<< HEAD
=======
    private final UserIntelligencePort intelligencePort;
>>>>>>> 8465ad1 (Se configuro correctamente el envio de notificaciones al momento de registrar un usuario con email o telefono duplicado)

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
        // 🔹 1. Correo duplicado
        if (repository.existsByEmail(domain.email())) {
            notificationService.notifyEmailConflict(RegistrationAttempt.fromUser(domain));
            throw DomainException.buildFromCatalog(
                MessageCodes.Domain.User.EMAIL_ALREADY_REGISTERED_TECHNICAL,
                MessageCodes.Domain.User.EMAIL_ALREADY_REGISTERED_USER
            );
        }

        // 🔹 2. Documento duplicado
        if (repository.existsByIdTypeAndIdNumber(domain.idType(), domain.idNumber())) {
            throw DomainException.buildFromCatalog(
                MessageCodes.Domain.User.ID_NUMBER_ALREADY_REGISTERED_TECHNICAL,
                MessageCodes.Domain.User.ID_NUMBER_ALREADY_REGISTERED_USER
            );
        }

        // 🔹 3. Teléfono duplicado
        if (repository.existsByMobileNumber(domain.mobileNumber())) {
            notificationService.notifyMobileConflict(RegistrationAttempt.fromUser(domain));
            throw DomainException.buildFromCatalog(
                MessageCodes.Domain.User.MOBILE_ALREADY_REGISTERED_TECHNICAL,
                MessageCodes.Domain.User.MOBILE_ALREADY_REGISTERED_USER
            );
        }
    }
}
