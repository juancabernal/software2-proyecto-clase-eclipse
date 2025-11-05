package co.edu.uco.ucochallenge.domain.verification.model;

import java.time.Instant;
import java.util.UUID;

import co.edu.uco.ucochallenge.crosscuting.exception.DomainException;
import co.edu.uco.ucochallenge.crosscuting.helper.ObjectHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.UUIDHelper;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageCodes;

public record VerificationToken(
        UUID id,
        String contact,
        String code,
        Instant expiration,
        int attempts,
        Instant createdAt) {

    public VerificationToken {
        id = normalizeId(id);
        contact = normalizeContact(contact);
        code = normalizeCode(code);
        expiration = ObjectHelper.getDefault(expiration, Instant.now());
        createdAt = ObjectHelper.getDefault(createdAt, Instant.now());
        attempts = Math.max(attempts, 0);
    }

    private static UUID normalizeId(final UUID id) {
        final UUID normalized = UUIDHelper.getDefault(id);
        if (UUIDHelper.getDefault().equals(normalized)) {
            return UUID.randomUUID();
        }
        return normalized;
    }

    private static String normalizeContact(final String contact) {
        final String normalized = TextHelper.getDefaultWithTrim(contact);
        if (TextHelper.isEmpty(normalized)) {
            throw DomainException.buildFromCatalog(
                    MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_TECHNICAL,
                    MessageCodes.Domain.Verification.TOKEN_NOT_FOUND_USER);
        }
        return normalized;
    }

    private static String normalizeCode(final String code) {
        final String normalized = TextHelper.getDefaultWithTrim(code);
        if (TextHelper.isEmpty(normalized)) {
            throw DomainException.buildFromCatalog(
                    MessageCodes.Domain.Verification.TOKEN_INVALID_TECHNICAL,
                    MessageCodes.Domain.Verification.TOKEN_INVALID_USER);
        }
        return normalized;
    }

    public boolean isExpired(final Instant reference) {
        final Instant instant = ObjectHelper.getDefault(reference, Instant.now());
        return expiration.isBefore(instant) || expiration.equals(instant);
    }

    public VerificationToken decrementAttempts() {
        final int nextAttempts = Math.max(attempts - 1, 0);
        return new VerificationToken(id, contact, code, expiration, nextAttempts, createdAt);
    }
}