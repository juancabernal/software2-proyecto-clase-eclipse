package co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.impl;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.VerifyEmailTokenUseCase;
import co.edu.uco.ucochallenge.crosscuting.exception.DomainException;
import co.edu.uco.ucochallenge.crosscuting.helper.ObjectHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.UUIDHelper;
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
    public void execute(final UUID userId, final UUID tokenId, final String rawCode, final LocalDateTime verificationDate) {
        final String code = TextHelper.getDefaultWithTrim(rawCode);
        if (TextHelper.isEmpty(code)) {
            throw DomainException.buildFromCatalog(
                    MessageCodes.Domain.Verification.TOKEN_INVALID_TECHNICAL,
                    MessageCodes.Domain.Verification.TOKEN_INVALID_USER);
        }

        final LocalDateTime referenceDate = ObjectHelper.getDefault(verificationDate, LocalDateTime.now());

        final UUID normalizedTokenId = UUIDHelper.getDefault(tokenId);
        if (UUIDHelper.getDefault().equals(normalizedTokenId)) {
            throw DomainException.buildFromCatalog(
                    MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_TECHNICAL,
                    MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_USER);
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
        final VerificationToken verificationToken = tokenRepository.findById(normalizedTokenId)
                .orElseThrow(() -> DomainException.buildFromCatalog(
                        MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_TECHNICAL,
                        MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_USER));

        if (!contact.equalsIgnoreCase(verificationToken.contact())) {
            throw DomainException.buildFromCatalog(
                    MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_TECHNICAL,
                    MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_USER);
        }

        if (verificationToken.isExpired(referenceDate)) {
            tokenRepository.deleteById(verificationToken.id());
            throw DomainException.buildFromCatalog(
                    MessageCodes.Domain.Verification.TOKEN_EXPIRED_TECHNICAL,
                    MessageCodes.Domain.Verification.TOKEN_EXPIRED_USER);
        }

        if (!verificationToken.code().equalsIgnoreCase(code)) {
            throw DomainException.buildFromCatalog(
                    MessageCodes.Domain.Verification.TOKEN_INVALID_TECHNICAL,
                    MessageCodes.Domain.Verification.TOKEN_INVALID_USER);
        }

        final User updatedUser = user.markEmailAsConfirmed();
        userRepository.save(updatedUser);
        tokenRepository.deleteByContact(contact);
    }
}
