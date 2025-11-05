package co.edu.uco.ucochallenge.application.notification;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.crosscuting.exception.DomainException;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.UUIDHelper;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageCodes;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageProvider;
import co.edu.uco.ucochallenge.crosscuting.parameter.ParameterCodes;
import co.edu.uco.ucochallenge.crosscuting.parameter.ParameterProvider;
import co.edu.uco.ucochallenge.domain.user.port.out.UserRepository;
import co.edu.uco.ucochallenge.domain.user.model.User;
import co.edu.uco.ucochallenge.domain.verification.model.VerificationToken;
import co.edu.uco.ucochallenge.domain.verification.port.out.VerificationTokenRepository;
import jakarta.transaction.Transactional;

@Service
public class VerificationTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerificationTokenService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_LENGTH = 6;
    private static final int DEFAULT_TTL_MINUTES = 5;
    private static final int DEFAULT_MAX_ATTEMPTS = 3;

    private final VerificationTokenRepository repository;
    private final DuplicateRegistrationNotificationService notificationService;
    private final UserRepository userRepository;
    
    public VerificationTokenService(
            final VerificationTokenRepository repository,
            final DuplicateRegistrationNotificationService notificationService,
            final UserRepository userRepository) {
        this.repository = repository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }
    
    @Transactional
    public ConfirmationResponseDTO generateToken(final User user, final VerificationChannel channel) {
        final String contact = resolveContact(user, channel);
        final int ttlSeconds = resolveTtlSeconds();
        final int maxAttempts = resolveMaxAttempts();
        
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime expiration = now.plusSeconds(ttlSeconds);
        final String code = generateCode();

        repository.deleteByContact(contact);
        final VerificationToken savedToken = repository
                .save(new VerificationToken(null, contact, code, expiration, maxAttempts, now));

        LOGGER.info("Generated verification token for user {} via {}", user.id(), channel.name());
        try {
            notifyUser(user, channel, code, ttlSeconds, maxAttempts);
        } catch (Exception ex) {
            LOGGER.error("⚠️ Failed to send notification for user {} (token saved anyway): {}", user.id(), ex.getMessage());
        }

        return new ConfirmationResponseDTO(ttlSeconds, savedToken.id());
    }

    @Transactional
    public VerificationAttemptResponseDTO validateToken(final User user,
            final VerificationChannel channel,
            final UUID tokenId,
            final String providedCode) {
        final String sanitizedCode = TextHelper.getDefaultWithTrim(providedCode);
        if (TextHelper.isEmpty(sanitizedCode)) {
            final String message = MessageProvider
                    .getMessage(MessageCodes.Domain.Verification.TOKEN_INVALID_USER);
            return new VerificationAttemptResponseDTO(false, false, 0,
                    isContactConfirmed(user, channel),
                    user.emailConfirmed() && user.mobileNumberConfirmed(),
                    message);
        }

        final UUID normalizedTokenId = UUIDHelper.getDefault(tokenId);
        if (UUIDHelper.getDefault().equals(normalizedTokenId)) {
            final String message = MessageProvider
                    .getMessage(MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_USER);
            return new VerificationAttemptResponseDTO(false, false, 0,
                    isContactConfirmed(user, channel),
                    user.emailConfirmed() && user.mobileNumberConfirmed(),
                    message);
        }

        final VerificationToken token = repository.findById(normalizedTokenId)
                .orElse(null);

        if (token == null) {
            final String message = MessageProvider
                    .getMessage(MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_USER);
            return new VerificationAttemptResponseDTO(false, false, 0,
                    isContactConfirmed(user, channel),
                    user.emailConfirmed() && user.mobileNumberConfirmed(),
                    message);
        }

        final String contact = resolveContact(user, channel);
        if (!contact.equalsIgnoreCase(token.contact())) {
            final String message = MessageProvider
                    .getMessage(MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_USER);
            return new VerificationAttemptResponseDTO(false, false, 0,
                    isContactConfirmed(user, channel),
                    user.emailConfirmed() && user.mobileNumberConfirmed(),
                    message);
        }

        if (token.isExpired(LocalDateTime.now())) {
            repository.deleteById(token.id());
            final String message = MessageProvider
                    .getMessage(MessageCodes.Domain.Verification.TOKEN_EXPIRED_USER);
            return new VerificationAttemptResponseDTO(false, true, 0,
                    isContactConfirmed(user, channel),
                    user.emailConfirmed() && user.mobileNumberConfirmed(),
                    message);
        }

        if (!token.code().equalsIgnoreCase(sanitizedCode)) {
            final VerificationToken decremented = token.decrementAttempts();
            if (decremented.attempts() <= 0) {
                repository.deleteById(token.id());
                final String message = MessageProvider
                        .getMessage(MessageCodes.Domain.Verification.TOKEN_ATTEMPTS_EXHAUSTED_USER);
                return new VerificationAttemptResponseDTO(false, false, 0,
                        isContactConfirmed(user, channel),
                        user.emailConfirmed() && user.mobileNumberConfirmed(),
                        message);
            }

            repository.save(decremented);
            final String message = MessageProvider
                    .getMessage(MessageCodes.Domain.Verification.TOKEN_INVALID_USER,
                            Map.of("attemptsRemaining", String.valueOf(decremented.attempts())));
            return new VerificationAttemptResponseDTO(false, false, decremented.attempts(),
                    isContactConfirmed(user, channel),
                    user.emailConfirmed() && user.mobileNumberConfirmed(),
                    message);
        }

        repository.deleteByContact(contact);

        if (isContactConfirmed(user, channel)) {
            final String message = MessageProvider
                    .getMessage(MessageCodes.Domain.Verification.CONTACT_ALREADY_CONFIRMED_USER);
            return new VerificationAttemptResponseDTO(true, false, token.attempts(),
                    true,
                    user.emailConfirmed() && user.mobileNumberConfirmed(),
                    message);
        }

        final User updatedUser = updateContactStatus(user, channel);
        final boolean allConfirmed = updatedUser.emailConfirmed() && updatedUser.mobileNumberConfirmed();
        if (allConfirmed) {
            purgeRemainingTokens(updatedUser);
        }

        final String message = MessageProvider
                .getMessage(MessageCodes.Domain.Verification.CONTACT_CONFIRMED_USER);
        return new VerificationAttemptResponseDTO(true, false, token.attempts(),
                true,
                allConfirmed,
                message);
    }

    private void notifyUser(final User user,
            final VerificationChannel channel,
            final String code,
            final int ttlSeconds,
            final int maxAttempts) {
        final RegistrationAttempt attempt = RegistrationAttempt.fromUser(user);
        final int ttlMinutes = Math.max(ttlSeconds / 60, 1);
        if (channel.isEmail()) {
            notificationService.notifyEmailConfirmation(attempt, code, ttlMinutes, maxAttempts);
        } else {
            notificationService.notifyMobileConfirmation(attempt, code, ttlMinutes, maxAttempts);
        }
    }

    private String resolveContact(final User user, final VerificationChannel channel) {
        if (channel.isEmail()) {
            final String email = TextHelper.getDefaultWithTrim(user.email());
            if (TextHelper.isEmpty(email)) {
                throw DomainException.buildFromCatalog(
                        MessageCodes.Domain.User.EMAIL_EMPTY_TECHNICAL,
                        MessageCodes.Domain.User.EMAIL_EMPTY_USER);
            }
            return email.toLowerCase(Locale.ROOT);
        }

        final String mobile = TextHelper.getDefaultWithTrim(user.mobileNumber());
        if (TextHelper.isEmpty(mobile)) {
            throw DomainException.buildFromCatalog(
                    MessageCodes.Domain.User.MOBILE_EMPTY_TECHNICAL,
                    MessageCodes.Domain.User.MOBILE_EMPTY_USER);
        }
        return mobile;
    }

    private boolean isContactConfirmed(final User user, final VerificationChannel channel) {
        return channel.isEmail() ? user.emailConfirmed() : user.mobileNumberConfirmed();
    }

    private User updateContactStatus(final User user, final VerificationChannel channel) {
        final User updated = channel.isEmail()
                ? new User(user.id(), user.idType(), user.idNumber(), user.firstName(), user.secondName(),
                        user.firstSurname(), user.secondSurname(), user.homeCity(), user.email(), user.mobileNumber(),
                        true, user.mobileNumberConfirmed())
                : new User(user.id(), user.idType(), user.idNumber(), user.firstName(), user.secondName(),
                        user.firstSurname(), user.secondSurname(), user.homeCity(), user.email(), user.mobileNumber(),
                        user.emailConfirmed(), true);
        return userRepository.save(updated);
    }

    private void purgeRemainingTokens(final User user) {
        final String email = TextHelper.getDefaultWithTrim(user.email());
        if (!TextHelper.isEmpty(email)) {
            repository.deleteByContact(email.toLowerCase(Locale.ROOT));
        }
        final String mobile = TextHelper.getDefaultWithTrim(user.mobileNumber());
        if (!TextHelper.isEmpty(mobile)) {
            repository.deleteByContact(mobile);
        }
    }

    private int resolveTtlSeconds() {
        int minutes = DEFAULT_TTL_MINUTES;
        try {
            final int configured = ParameterProvider
                    .getInteger(ParameterCodes.Verification.VERIFICATION_CODE_EXPIRATION_MINUTES);
            if (configured > 0) {
                minutes = configured;
            }
        } catch (final RuntimeException exception) {
            LOGGER.warn("Falling back to default verification token TTL of {} minutes due to parameter retrieval error.",
                    DEFAULT_TTL_MINUTES, exception);
        }
        return Math.max(minutes, 1) * 60;
    }
    
    private int resolveMaxAttempts() {
        int attempts = DEFAULT_MAX_ATTEMPTS;
        try {
            final int configured = ParameterProvider
                    .getInteger(ParameterCodes.Verification.VERIFICATION_CODE_MAX_ATTEMPTS);
            if (configured > 0) {
                attempts = configured;
            }
        } catch (final RuntimeException exception) {
            LOGGER.warn("Falling back to default verification token attempts of {} due to parameter retrieval error.",
                    DEFAULT_MAX_ATTEMPTS, exception);
        }
        return Math.max(attempts, 1);
    }

    private String generateCode() {
        final int number = RANDOM.nextInt((int) Math.pow(10, TOKEN_LENGTH));
        return String.format(Locale.ROOT, "%0" + TOKEN_LENGTH + "d", number);
    }
}
