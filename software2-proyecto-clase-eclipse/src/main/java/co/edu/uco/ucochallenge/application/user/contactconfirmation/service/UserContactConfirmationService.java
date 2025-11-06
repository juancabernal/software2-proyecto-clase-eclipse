package co.edu.uco.ucochallenge.application.user.contactconfirmation.service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.notification.VerificationChannel;
import co.edu.uco.ucochallenge.application.notification.VerificationTokenService;
import co.edu.uco.ucochallenge.application.notification.VerificationAttemptResponseDTO;
import co.edu.uco.ucochallenge.crosscuting.exception.DomainException;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageCodes;
import co.edu.uco.ucochallenge.domain.user.model.User;
import co.edu.uco.ucochallenge.domain.user.port.out.UserRepository;
import co.edu.uco.ucochallenge.domain.verification.model.VerificationToken;
import co.edu.uco.ucochallenge.domain.verification.port.out.VerificationTokenRepository;

@Service
public class UserContactConfirmationService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final VerificationTokenService verificationTokenService;

    public UserContactConfirmationService(final UserRepository userRepository,
            final VerificationTokenRepository verificationTokenRepository,
            final VerificationTokenService verificationTokenService) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.verificationTokenService = verificationTokenService;
    }

    public void confirmVerificationCode(final UUID userId, final VerificationChannel channel, final String rawCode) {
        final String sanitizedCode = TextHelper.getDefaultWithTrim(rawCode);
        if (TextHelper.isEmpty(sanitizedCode)) {
            throw DomainException.buildFromCatalog(
                    MessageCodes.Domain.Verification.TOKEN_INVALID_TECHNICAL,
                    MessageCodes.Domain.Verification.TOKEN_INVALID_USER);
        }

        final User user = userRepository.findById(userId)
                .orElseThrow(() -> DomainException.buildFromCatalog(
                        MessageCodes.Domain.User.NOT_FOUND_TECHNICAL,
                        MessageCodes.Domain.User.NOT_FOUND_USER));

        final String contact = resolveContact(user, channel);
        final VerificationToken token = verificationTokenRepository.findByContact(contact)
                .orElseThrow(() -> DomainException.buildFromCatalog(
                        MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_TECHNICAL,
                        MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_USER));

        final VerificationAttemptResponseDTO result = verificationTokenService.validateToken(
                user,
                channel,
                token.id(),
                sanitizedCode,
                LocalDateTime.now());

        if (!result.success()) {
            if (result.expired()) {
                throw DomainException.buildFromCatalog(
                        MessageCodes.Domain.Verification.TOKEN_EXPIRED_TECHNICAL,
                        MessageCodes.Domain.Verification.TOKEN_EXPIRED_USER);
            }
            throw DomainException.buildFromCatalog(
                    MessageCodes.Domain.Verification.TOKEN_INVALID_TECHNICAL,
                    MessageCodes.Domain.Verification.TOKEN_INVALID_USER);
        }
    }

    private String resolveContact(final User user, final VerificationChannel channel) {
        if (channel.isEmail()) {
            final String email = TextHelper.getDefaultWithTrim(user.email()).toLowerCase(Locale.ROOT);
            if (TextHelper.isEmpty(email)) {
                throw DomainException.buildFromCatalog(
                        MessageCodes.Domain.User.EMAIL_EMPTY_TECHNICAL,
                        MessageCodes.Domain.User.EMAIL_EMPTY_USER);
            }
            return email;
        }

        final String mobile = TextHelper.getDefaultWithTrim(user.mobileNumber());
        if (TextHelper.isEmpty(mobile)) {
            throw DomainException.buildFromCatalog(
                    MessageCodes.Domain.User.MOBILE_EMPTY_TECHNICAL,
                    MessageCodes.Domain.User.MOBILE_EMPTY_USER);
        }
        return mobile;
    }
}
