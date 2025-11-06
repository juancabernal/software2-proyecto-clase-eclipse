package co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.notification.VerificationAttemptResponseDTO;
import co.edu.uco.ucochallenge.application.notification.VerificationChannel;
import co.edu.uco.ucochallenge.application.notification.VerificationTokenService;
import co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.ConfirmVerificationCodeUseCase;
import co.edu.uco.ucochallenge.crosscuting.exception.DomainException;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageCodes;
import co.edu.uco.ucochallenge.domain.user.model.User;
import co.edu.uco.ucochallenge.domain.user.port.out.UserRepository;

@Service
public class ConfirmVerificationCodeUseCaseImpl implements ConfirmVerificationCodeUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfirmVerificationCodeUseCaseImpl.class);

    private final UserRepository repository;
    private final VerificationTokenService verificationTokenService;

    public ConfirmVerificationCodeUseCaseImpl(final UserRepository repository,
            final VerificationTokenService verificationTokenService) {
        this.repository = repository;
        this.verificationTokenService = verificationTokenService;
    }

    @Override
    public VerificationAttemptResponseDTO execute(final UUID userId,
            final VerificationChannel channel,
            final String code) {
        final User user = repository.findById(userId)
                .orElseThrow(() -> DomainException.buildFromCatalog(
                        MessageCodes.Domain.User.NOT_FOUND_TECHNICAL,
                        MessageCodes.Domain.User.NOT_FOUND_USER));

        LOGGER.info("Validating verification code for user {} via {}", user.id(), channel);
        return verificationTokenService.validateTokenByContact(user, channel, code, LocalDateTime.now());
    }
}
