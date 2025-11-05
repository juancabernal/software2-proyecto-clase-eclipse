package co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.notification.VerificationChannel;
import co.edu.uco.ucochallenge.application.notification.VerificationTokenService;
import co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.ConfirmMobileByTokenUseCase;
import co.edu.uco.ucochallenge.crosscuting.exception.DomainException;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageCodes;
import co.edu.uco.ucochallenge.domain.user.model.User;
import co.edu.uco.ucochallenge.domain.user.port.out.UserRepository;

@Service
public class ConfirmMobileByTokenUseCaseImpl implements ConfirmMobileByTokenUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfirmMobileByTokenUseCaseImpl.class);

    private final UserRepository repository;
    private final VerificationTokenService verificationTokenService;

    public ConfirmMobileByTokenUseCaseImpl(final UserRepository repository,
            final VerificationTokenService verificationTokenService) {
        this.repository = repository;
        this.verificationTokenService = verificationTokenService;
    }

    @Override
    public void execute(final UUID userId, final String token) {
        final User user = repository.findById(userId)
                .orElseThrow(() -> DomainException.buildFromCatalog(
                        MessageCodes.Domain.User.NOT_FOUND_TECHNICAL,
                        MessageCodes.Domain.User.NOT_FOUND_USER));

        LOGGER.info("Confirming mobile contact for user {} using verification token", user.id());
        verificationTokenService.confirmToken(user, VerificationChannel.MOBILE, token);
    }
}
