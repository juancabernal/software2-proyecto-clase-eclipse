package co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.notification.VerificationAttemptResponseDTO;
import co.edu.uco.ucochallenge.application.notification.VerificationChannel;
import co.edu.uco.ucochallenge.application.notification.VerificationTokenService;
import co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.ValidateEmailConfirmationUseCase;
import co.edu.uco.ucochallenge.crosscuting.exception.DomainException;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageCodes;
import co.edu.uco.ucochallenge.domain.user.model.User;
import co.edu.uco.ucochallenge.domain.user.port.out.UserRepository;

@Service
public class ValidateEmailConfirmationUseCaseImpl implements ValidateEmailConfirmationUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateEmailConfirmationUseCaseImpl.class);

    private final UserRepository repository;
    private final VerificationTokenService verificationTokenService;

    public ValidateEmailConfirmationUseCaseImpl(final UserRepository repository,
            final VerificationTokenService verificationTokenService) {
        this.repository = repository;
        this.verificationTokenService = verificationTokenService;
    }

    @Override
    public VerificationAttemptResponseDTO execute(final UUID userId,
            final UUID tokenId,
            final String code,
            final LocalDateTime verificationDate) {
        final User user = repository.findById(userId)
                .orElseThrow(() -> DomainException.buildFromCatalog(
                        MessageCodes.Domain.User.NOT_FOUND_TECHNICAL,
                        MessageCodes.Domain.User.NOT_FOUND_USER));

        LOGGER.info("Validating email confirmation token for user {}", user.id());
        return verificationTokenService.validateToken(user, VerificationChannel.EMAIL, tokenId, code, verificationDate);
    }
}