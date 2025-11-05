package co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.VerifyMobileTokenUseCase;
import co.edu.uco.ucochallenge.crosscuting.exception.DomainException;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.UUIDHelper;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageCodes;
import co.edu.uco.ucochallenge.domain.user.model.User;
import co.edu.uco.ucochallenge.domain.user.port.out.UserRepository;
import co.edu.uco.ucochallenge.domain.verification.model.VerificationToken;
import co.edu.uco.ucochallenge.domain.verification.port.out.VerificationTokenRepository;

@Service
@Transactional
public class VerifyMobileTokenUseCaseImpl implements VerifyMobileTokenUseCase {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;

    public VerifyMobileTokenUseCaseImpl(
            final UserRepository userRepository,
            final VerificationTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    @Override
    public void execute(final UUID userId, final UUID tokenId, final String rawCode) {
        final String code = TextHelper.getDefaultWithTrim(rawCode);
        if (TextHelper.isEmpty(code)) {
            throw DomainException.buildFromCatalog(
                    MessageCodes.Domain.Verification.TOKEN_INVALID_TECHNICAL,
                    MessageCodes.Domain.Verification.TOKEN_INVALID_USER);
        }

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

        if (user.mobileNumberConfirmed()) {
            throw DomainException.buildFromCatalog(
                    MessageCodes.Domain.User.MOBILE_ALREADY_CONFIRMED_TECHNICAL,
                    MessageCodes.Domain.User.MOBILE_ALREADY_CONFIRMED_USER);
        }

        final String contact = TextHelper.getDefaultWithTrim(user.mobileNumber());
        final VerificationToken verificationToken = tokenRepository.findById(normalizedTokenId)
                .orElseThrow(() -> DomainException.buildFromCatalog(
                        MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_TECHNICAL,
                        MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_USER));

        if (!contact.equalsIgnoreCase(verificationToken.contact())) {
            throw DomainException.buildFromCatalog(
                    MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_TECHNICAL,
                    MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_USER);
        }

        if (verificationToken.isExpired(LocalDateTime.now())) {
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

        final User updatedUser = user.markMobileNumberAsConfirmed();
        userRepository.save(updatedUser);
        tokenRepository.deleteByContact(contact);
    }
}
