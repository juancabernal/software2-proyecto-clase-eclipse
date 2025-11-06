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
import co.edu.uco.ucochallenge.crosscuting.helper.ObjectHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.UUIDHelper;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageCodes;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageProvider;
import co.edu.uco.ucochallenge.crosscuting.parameter.ParameterCodes;
import co.edu.uco.ucochallenge.crosscuting.parameter.ParameterProvider;
import co.edu.uco.ucochallenge.domain.pagination.PageCriteria; // ✅ FIX: Support user lookup by contact for public verification
import co.edu.uco.ucochallenge.domain.pagination.PaginatedResult; // ✅ FIX: Handle paginated repository responses for contact lookup
import co.edu.uco.ucochallenge.domain.user.port.out.UserRepository;
import co.edu.uco.ucochallenge.domain.user.model.User;
import co.edu.uco.ucochallenge.domain.user.model.UserFilter; // ✅ FIX: Filter users by contact when resolving tokens
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
        LOGGER.info("Token generado: id={}, contact={}, channel={}, ttl={}",
                savedToken.id(), contact, channel.name(), ttlSeconds);

        return new ConfirmationResponseDTO(
                savedToken.id(),   // UUID del token generado
                contact,           // correo o móvil
                channel.name(),    // EMAIL o MOBILE
                ttlSeconds         // segundos de expiración
        );

    }

    @Transactional
    public VerificationAttemptResponseDTO validateToken(final User user,
            final VerificationChannel channel,
            final UUID tokenId,
            final String providedCode,
            final LocalDateTime verificationDate) {
        final String sanitizedCode = TextHelper.getDefaultWithTrim(providedCode);
        if (TextHelper.isEmpty(sanitizedCode)) {
            final String message = MessageProvider
                    .getMessage(MessageCodes.Domain.Verification.TOKEN_INVALID_USER);
            return new VerificationAttemptResponseDTO(false, false, 0,
                    isContactConfirmed(user, channel),
                    user.emailConfirmed() && user.mobileNumberConfirmed(),
                    message,
                    null); // ✅ FIX: Propagate null verification identifier on empty code rejection
        }

        final LocalDateTime referenceDate = ObjectHelper.getDefault(verificationDate, LocalDateTime.now());

        final UUID normalizedTokenId = UUIDHelper.getDefault(tokenId);
        if (UUIDHelper.getDefault().equals(normalizedTokenId)) {
            final String message = MessageProvider
                    .getMessage(MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_USER);
            return new VerificationAttemptResponseDTO(false, false, 0,
                    isContactConfirmed(user, channel),
                    user.emailConfirmed() && user.mobileNumberConfirmed(),
                    message,
                    normalizedTokenId); // ✅ FIX: Return normalized identifier for client-side traceability
        }

        final VerificationToken token = repository.findById(normalizedTokenId)
                .orElse(null);

        if (token == null) {
            final String message = MessageProvider
                    .getMessage(MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_USER);
            return new VerificationAttemptResponseDTO(false, false, 0,
                    isContactConfirmed(user, channel),
                    user.emailConfirmed() && user.mobileNumberConfirmed(),
                    message,
                    normalizedTokenId); // ✅ FIX: Keep identifier when token not found for auditing
        }

        final String contact = resolveContact(user, channel);
        if (!contact.equalsIgnoreCase(token.contact())) {
            final String message = MessageProvider
                    .getMessage(MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_USER);
            return new VerificationAttemptResponseDTO(false, false, 0,
                    isContactConfirmed(user, channel),
                    user.emailConfirmed() && user.mobileNumberConfirmed(),
                    message,
                    token.id()); // ✅ FIX: Surface identifier when contact mismatch occurs
        }

        if (token.isExpired(referenceDate)) {
            repository.deleteById(token.id());
            final String message = MessageProvider
                    .getMessage(MessageCodes.Domain.Verification.TOKEN_EXPIRED_USER);
            return new VerificationAttemptResponseDTO(false, true, 0,
                    isContactConfirmed(user, channel),
                    user.emailConfirmed() && user.mobileNumberConfirmed(),
                    message,
                    token.id()); // ✅ FIX: Notify frontend about expired identifier for follow-up requests
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
                        message,
                        token.id()); // ✅ FIX: Attach identifier when attempts are exhausted for logging
            }

            repository.save(decremented);
            final String message = MessageProvider
                    .getMessage(MessageCodes.Domain.Verification.TOKEN_INVALID_USER,
                            Map.of("attemptsRemaining", String.valueOf(decremented.attempts())));
            return new VerificationAttemptResponseDTO(false, false, decremented.attempts(),
                    isContactConfirmed(user, channel),
                    user.emailConfirmed() && user.mobileNumberConfirmed(),
                    message,
                    token.id()); // ✅ FIX: Keep identifier when attempts remain for retries
        }

        repository.deleteByContact(contact);

        if (isContactConfirmed(user, channel)) {
            final String message = MessageProvider
                    .getMessage(MessageCodes.Domain.Verification.CONTACT_ALREADY_CONFIRMED_USER);
            return new VerificationAttemptResponseDTO(true, false, token.attempts(),
                    true,
                    user.emailConfirmed() && user.mobileNumberConfirmed(),
                    message,
                    token.id()); // ✅ FIX: Return identifier even when contact already confirmed
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
                message,
                token.id()); // ✅ FIX: Provide identifier to confirm successful validation
    }

    @Transactional
    public VerificationAttemptResponseDTO validateTokenByContact(final User user,
            final VerificationChannel channel,
            final String providedCode,
            final LocalDateTime verificationDate) {
        final String contact = resolveContact(user, channel);
        final VerificationToken token = repository.findByContact(contact)
                .orElse(null);

        if (token == null) {
            final String message = MessageProvider
                    .getMessage(MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_USER);
            return new VerificationAttemptResponseDTO(false, false, 0,
                    isContactConfirmed(user, channel),
                    user.emailConfirmed() && user.mobileNumberConfirmed(),
                    message,
                    null);
        }

        return validateToken(user, channel, token.id(), providedCode, verificationDate);
    }

    @Transactional // ✅ FIX: Ensure link-based verification updates run within a transaction
    public VerificationAttemptResponseDTO validateTokenViaPublicId(final UUID tokenId) { // ✅ FIX: Enable verification link consumption without user context
        final UUID normalizedTokenId = UUIDHelper.getDefault(tokenId); // ✅ FIX: Normalize incoming identifier before processing
        if (UUIDHelper.getDefault().equals(normalizedTokenId)) { // ✅ FIX: Guard against missing public identifier
            final String message = MessageProvider // ✅ FIX: Reuse catalog message for invalid identifiers
                    .getMessage(MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_USER); // ✅ FIX: Provide user-friendly feedback
            return new VerificationAttemptResponseDTO(false, false, 0, // ✅ FIX: Return default failure payload for invalid identifiers
                    false, // ✅ FIX: Indicate contact remains unconfirmed when identifier invalid
                    false, // ✅ FIX: Indicate overall confirmation still pending
                    message, // ✅ FIX: Surface localized message for frontend display
                    normalizedTokenId); // ✅ FIX: Echo normalized identifier for diagnostics
        }

        final VerificationToken token = repository.findById(normalizedTokenId).orElse(null); // ✅ FIX: Fetch persisted token information
        if (token == null) { // ✅ FIX: Handle missing tokens gracefully
            final String message = MessageProvider // ✅ FIX: Maintain consistent messaging for missing tokens
                    .getMessage(MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_USER); // ✅ FIX: Provide user-friendly feedback for missing token
            return new VerificationAttemptResponseDTO(false, false, 0, // ✅ FIX: Return failure structure for absent tokens
                    false, // ✅ FIX: Indicate contact remains pending when token absent
                    false, // ✅ FIX: Keep aggregate confirmation status pending
                    message, // ✅ FIX: Return descriptive message for UI usage
                    normalizedTokenId); // ✅ FIX: Preserve identifier for debugging traces
        }

        final VerificationChannel channel = inferChannel(token.contact()); // ✅ FIX: Derive channel from stored contact value
        final User user = resolveUserByContact(token.contact(), channel); // ✅ FIX: Locate owning user based on contact
        if (user == null) { // ✅ FIX: Handle orphaned tokens without associated users
            final String message = MessageProvider // ✅ FIX: Inform about missing user association
                    .getMessage(MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_USER); // ✅ FIX: Reuse existing user-facing message
            return new VerificationAttemptResponseDTO(false, false, 0, // ✅ FIX: Return failure payload when user not located
                    false, // ✅ FIX: Signal contact remains unconfirmed when user missing
                    false, // ✅ FIX: Keep global confirmation false without user context
                    message, // ✅ FIX: Provide message for UI feedback
                    token.id()); // ✅ FIX: Return identifier bound to orphaned token for auditing
        }

        return validateToken(user, channel, token.id(), token.code(), LocalDateTime.now()); // ✅ FIX: Reuse existing validation logic with stored code
    }

    private VerificationChannel inferChannel(final String contact) { // ✅ FIX: Determine verification channel from contact format
        if (contact != null && contact.contains("@")) { // ✅ FIX: Detect email contacts using simple heuristic
            return VerificationChannel.EMAIL; // ✅ FIX: Map email contacts to email channel
        }
        return VerificationChannel.MOBILE; // ✅ FIX: Fallback to mobile channel for numeric contacts
    }

    private User resolveUserByContact(final String contact, final VerificationChannel channel) { // ✅ FIX: Locate user owning the verification token
        if (TextHelper.isEmpty(contact)) { // ✅ FIX: Protect against empty contact values
            return null; // ✅ FIX: Abort lookup when contact missing
        }
        try { // ✅ FIX: Guard against filter construction errors
            final UserFilter filter = channel.isEmail() // ✅ FIX: Build user filter according to contact type
                    ? new UserFilter(null, null, null, null, null, contact, null, null) // ✅ FIX: Filter users by email contact
                    : new UserFilter(null, null, null, null, null, null, contact, null); // ✅ FIX: Filter users by mobile number contact
            final PaginatedResult<User> result = userRepository.findByFilter(filter, PageCriteria.of(0, 1)); // ✅ FIX: Query repository for matching user
            return result.items().stream().findFirst().orElse(null); // ✅ FIX: Retrieve first matching user if present
        } catch (final Exception exception) { // ✅ FIX: Prevent unexpected filter errors from breaking flow
            LOGGER.error("Failed to resolve user for contact {}: {}", contact, exception.getMessage()); // ✅ FIX: Log lookup failure for diagnostics
            return null; // ✅ FIX: Fallback to null when lookup fails
        }
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
