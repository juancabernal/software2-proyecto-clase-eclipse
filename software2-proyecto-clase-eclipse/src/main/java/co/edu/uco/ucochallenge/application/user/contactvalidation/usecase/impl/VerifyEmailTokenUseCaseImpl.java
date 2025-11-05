package co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.impl;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.VerifyEmailTokenUseCase;
import co.edu.uco.ucochallenge.crosscuting.exception.DomainException;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageCodes;
import co.edu.uco.ucochallenge.domain.user.model.User;
import co.edu.uco.ucochallenge.domain.user.port.out.UserRepository;
import co.edu.uco.ucochallenge.domain.verification.model.VerificationToken;
import co.edu.uco.ucochallenge.domain.verification.port.out.VerificationTokenRepository;

@Service
@Transactional
public class VerifyEmailTokenUseCaseImpl implements VerifyEmailTokenUseCase {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;

    public VerifyEmailTokenUseCaseImpl(
            final UserRepository userRepository,
            final VerificationTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    @Override
    public void execute(final UUID userId, final String rawToken) {
        final String token = TextHelper.getDefaultWithTrim(rawToken);
        if (TextHelper.isEmpty(token)) {
            throw DomainException.buildFromCatalog(
                    MessageCodes.Domain.Verification.TOKEN_INVALID_TECHNICAL,
                    MessageCodes.Domain.Verification.TOKEN_INVALID_USER);
        }

        final User user = userRepository.findById(userId)
                .orElseThrow(() -> DomainException.buildFromCatalog(
                        MessageCodes.Domain.User.NOT_FOUND_TECHNICAL,
                        MessageCodes.Domain.User.NOT_FOUND_USER));

        if (user.emailConfirmed()) {
            throw DomainException.buildFromCatalog(
                    MessageCodes.Domain.User.EMAIL_ALREADY_CONFIRMED_TECHNICAL,
                    MessageCodes.Domain.User.EMAIL_ALREADY_CONFIRMED_USER);
        }

        final String contact = TextHelper.getDefaultWithTrim(user.email()).toLowerCase(Locale.ROOT);
        final VerificationToken verificationToken = tokenRepository.findByContact(contact)
                .orElseThrow(() -> DomainException.buildFromCatalog(
                        MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_TECHNICAL,
                        MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_USER));

        if (verificationToken.isExpired(LocalDateTime.now())) {
            tokenRepository.deleteByContact(contact);
            throw DomainException.buildFromCatalog(
                    MessageCodes.Domain.Verification.TOKEN_EXPIRED_TECHNICAL,
                    MessageCodes.Domain.Verification.TOKEN_EXPIRED_USER);
        }

        if (!verificationToken.code().equalsIgnoreCase(token)) {
            throw DomainException.buildFromCatalog(
                    MessageCodes.Domain.Verification.TOKEN_INVALID_TECHNICAL,
                    MessageCodes.Domain.Verification.TOKEN_INVALID_USER);
        }

        final User updatedUser = user.markEmailAsConfirmed();
        userRepository.save(updatedUser);
        tokenRepository.deleteByContact(contact);
    }
}
