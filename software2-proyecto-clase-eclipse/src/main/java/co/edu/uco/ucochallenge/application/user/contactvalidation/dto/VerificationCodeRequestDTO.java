package co.edu.uco.ucochallenge.application.user.contactvalidation.dto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.UUIDHelper;

public record VerificationCodeRequestDTO(String code, String token, String verifiedAt) {

    public String sanitizedCode() {
        return TextHelper.getDefaultWithTrim(code);
    }

    public UUID sanitizedTokenId() {
        final String rawToken = TextHelper.getDefaultWithTrim(token);
        if (TextHelper.isEmpty(rawToken)) {
            return null;
        }
        try {
            final UUID parsed = UUIDHelper.getFromString(rawToken);
            return UUIDHelper.getDefault().equals(parsed) ? null : parsed;
        } catch (final IllegalArgumentException exception) {
            return null;
        }
    }

    public LocalDateTime sanitizedVerificationDate() {
        final String rawDate = TextHelper.getDefaultWithTrim(verifiedAt);
        if (TextHelper.isEmpty(rawDate)) {
            return LocalDateTime.now();
        }
        try {
            return OffsetDateTime.parse(rawDate).toLocalDateTime();
        } catch (final DateTimeParseException ignored) {
            try {
                return LocalDateTime.parse(rawDate);
            } catch (final DateTimeParseException ignoredAgain) {
                return LocalDateTime.now();
            }
        }
    }
}
