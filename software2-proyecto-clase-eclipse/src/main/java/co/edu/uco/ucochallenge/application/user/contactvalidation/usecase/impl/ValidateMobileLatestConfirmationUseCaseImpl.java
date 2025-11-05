package co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.notification.VerificationAttemptResponseDTO;
import co.edu.uco.ucochallenge.application.notification.VerificationChannel;
import co.edu.uco.ucochallenge.application.notification.VerificationTokenService;
import co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.ValidateMobileLatestConfirmationUseCase;
import co.edu.uco.ucochallenge.crosscuting.exception.DomainException;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageCodes;
import co.edu.uco.ucochallenge.domain.user.model.User;
import co.edu.uco.ucochallenge.domain.user.port.out.UserRepository;

@Service
public class ValidateMobileLatestConfirmationUseCaseImpl implements ValidateMobileLatestConfirmationUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateMobileLatestConfirmationUseCaseImpl.class);

    private final UserRepository repository;
    private final VerificationTokenService verificationTokenService;

    public ValidateMobileLatestConfirmationUseCaseImpl(final UserRepository repository,
            final VerificationTokenService verificationTokenService) {
        this.repository = repository;
        this.verificationTokenService = verificationTokenService;
    }

    @Override
    public VerificationAttemptResponseDTO execute(final UUID userId, final String code) {
        final User user = repository.findById(userId)
                .orElseThrow(() -> DomainException.buildFromCatalog(
                        MessageCodes.Domain.User.NOT_FOUND_TECHNICAL,
                        MessageCodes.Domain.User.NOT_FOUND_USER));

        LOGGER.info("Validating mobile confirmation against the latest token for user {}", user.id());
        return verificationTokenService.validateLatestToken(user, VerificationChannel.MOBILE, code);
    }
}
