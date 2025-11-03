package co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.Void;
import co.edu.uco.ucochallenge.application.notification.DuplicateRegistrationNotificationService;
import co.edu.uco.ucochallenge.application.notification.RegistrationAttempt;
import co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.RequestEmailConfirmationUseCase;
import co.edu.uco.ucochallenge.crosscuting.exception.DomainException;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageCodes;
import co.edu.uco.ucochallenge.domain.user.model.User;
import co.edu.uco.ucochallenge.domain.user.port.out.UserRepository;

@Service
public class RequestEmailConfirmationUseCaseImpl implements RequestEmailConfirmationUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestEmailConfirmationUseCaseImpl.class);

    private final UserRepository repository;
    private final DuplicateRegistrationNotificationService notificationService;

    public RequestEmailConfirmationUseCaseImpl(final UserRepository repository,
            final DuplicateRegistrationNotificationService notificationService) {
        this.repository = repository;
        this.notificationService = notificationService;
    }

    @Override
    public Void execute(final UUID userId) {
        final User user = repository.findById(userId)
                .orElseThrow(() -> DomainException.buildFromCatalog(
                        MessageCodes.Domain.User.NOT_FOUND_TECHNICAL,
                        MessageCodes.Domain.User.NOT_FOUND_USER));

        if (user.emailConfirmed()) {
            throw DomainException.buildFromCatalog(
                    MessageCodes.Domain.User.EMAIL_ALREADY_CONFIRMED_TECHNICAL,
                    MessageCodes.Domain.User.EMAIL_ALREADY_CONFIRMED_USER);
        }

        LOGGER.info("Dispatching email confirmation request for user {}", user.id());
        notificationService.notifyEmailConfirmation(RegistrationAttempt.fromUser(user));
        return Void.returnVoid();
    }
}
