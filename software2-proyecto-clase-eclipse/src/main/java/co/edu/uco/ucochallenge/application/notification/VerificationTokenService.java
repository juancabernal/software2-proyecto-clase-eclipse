package co.edu.uco.ucochallenge.application.notification;

import java.security.SecureRandom;
import java.time.LocalDateTime;  // Se añade esta importación
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
        
        // Cambiar a LocalDateTime para usar la hora local
        final LocalDateTime now = LocalDateTime.now();  // Hora local
        // Calculamos la expiración en función de la hora local
        final LocalDateTime expiration = now.plusSeconds(ttlSeconds);  // Expiración sumando los segundos de TTL
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
            final UUID providedTokenId,
            final String providedCode) {
        final UUID tokenId = UUIDHelper.getDefault(providedTokenId);
        LOGGER.info("🔍 Intento de validación recibido para el usuario {} a través de {} con token {}",
                user.id(), channel.name(), tokenId);
        if (UUIDHelper.getDefault().equals(tokenId)) {
            LOGGER.info(
                    "ℹ️ No se recibió un identificador de token válido para el usuario {} en el canal {}. Se consultará el último token registrado.",
                    user.id(), channel.name());
        }

        final String sanitizedCode = TextHelper.getDefaultWithTrim(providedCode);
        LOGGER.debug("🔍 Código recibido para el usuario {} en el canal {}: {}", user.id(), channel.name(),
                sanitizedCode);
        if (TextHelper.isEmpty(sanitizedCode)) {
            LOGGER.warn("❌ Se recibió un código vacío para la validación del usuario {} en el canal {}", user.id(),
                    channel.name());
            final String message = MessageProvider
                    .getMessage(MessageCodes.Domain.Verification.TOKEN_INVALID_USER);
            return buildFailureResponse(user, channel, message, false, 0);
        }

        final String contact = resolveContact(user, channel);
        final VerificationToken token = findTokenForValidation(contact, tokenId);

        if (token == null) {
            LOGGER.warn("❌ No se encontró un token vigente para el contacto {}", contact);
            final String message = MessageProvider
                    .getMessage(MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_USER);
            return buildFailureResponse(user, channel, message, false, 0);
        }

        final LocalDateTime now = LocalDateTime.now();
        if (token.isExpired(now)) {
            LOGGER.warn("⌛ El token {} expiró antes de completar la validación para el contacto {}", token.id(),
                    contact);
            repository.deleteById(token.id());
            final String message = MessageProvider
                    .getMessage(MessageCodes.Domain.Verification.TOKEN_EXPIRED_USER);
            return buildFailureResponse(user, channel, message, true, 0);
        }

        if (!token.code().equalsIgnoreCase(sanitizedCode)) {
            LOGGER.warn("❌ Código inválido para el token {}. Intentos restantes antes de decrementar: {}", token.id(),
                    token.attempts());
            final VerificationToken decremented = token.decrementAttempts();
            if (decremented.attempts() <= 0) {
                LOGGER.warn("⛔ Se agotaron los intentos para el token {} del contacto {}", token.id(), contact);
                repository.deleteById(token.id());
                final String message = MessageProvider
                        .getMessage(MessageCodes.Domain.Verification.TOKEN_ATTEMPTS_EXHAUSTED_USER);
                return buildFailureResponse(user, channel, message, false, 0);
            }

            repository.save(decremented);
            final String message = MessageProvider
                    .getMessage(MessageCodes.Domain.Verification.TOKEN_INVALID_USER,
                            Map.of("attemptsRemaining", String.valueOf(decremented.attempts())));
            return buildFailureResponse(user, channel, message, false, decremented.attempts());
        }

        repository.deleteById(token.id());
        LOGGER.info("✅ Token {} validado correctamente para el contacto {}", token.id(), contact);

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

    public VerificationAttemptResponseDTO validateLatestToken(final User user,
            final VerificationChannel channel,
            final String providedCode) {
        LOGGER.debug("🔄 Solicitando validación contra el token más reciente para el usuario {} y canal {}",
                user.id(), channel.name());
        return validateToken(user, channel, UUIDHelper.getDefault(), providedCode);
    }

    private VerificationAttemptResponseDTO buildFailureResponse(final User user,
            final VerificationChannel channel,
            final String message,
            final boolean expired,
            final int attemptsRemaining) {
        return new VerificationAttemptResponseDTO(false, expired, attemptsRemaining,
                isContactConfirmed(user, channel),
                user.emailConfirmed() && user.mobileNumberConfirmed(),
                message);
    }

    private VerificationToken findTokenForValidation(final String contact, final UUID providedTokenId) {
        final UUID tokenId = UUIDHelper.getDefault(providedTokenId);

        if (!UUIDHelper.getDefault().equals(tokenId)) {
            final VerificationToken byId = repository.findById(tokenId)
                    .orElse(null);

            if (byId != null) {
                if (byId.contact().equalsIgnoreCase(contact)) {
                    LOGGER.debug("🔎 Token {} obtenido directamente por identificador para el contacto {}", tokenId, contact);
                    return byId;
                }

                LOGGER.warn(
                        "⚠️ El token {} recuperado por identificador no pertenece al contacto {}. Se consultará el último token por contacto.",
                        tokenId, contact);
            } else {
                LOGGER.warn(
                        "⚠️ No se encontró el token {} mediante su identificador. Se consultará el último token por contacto {}.",
                        tokenId, contact);
            }
        }

        final VerificationToken latest = repository.findByContact(contact)
                .orElse(null);

        if (latest != null) {
            LOGGER.debug("🔎 Token {} obtenido como el último registrado para el contacto {}", latest.id(), contact);
        }

        return latest;
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
