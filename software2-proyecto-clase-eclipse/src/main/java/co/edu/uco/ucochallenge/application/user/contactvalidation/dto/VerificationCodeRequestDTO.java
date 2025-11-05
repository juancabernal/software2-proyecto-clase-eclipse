package co.edu.uco.ucochallenge.application.user.contactvalidation.dto;

import java.util.UUID;

import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.UUIDHelper;

public record VerificationCodeRequestDTO(String code, String tokenId) {

    public String sanitizedCode() {
        return TextHelper.getDefaultWithTrim(code);
    }

    public UUID sanitizedTokenId() {
        final String rawToken = TextHelper.getDefaultWithTrim(tokenId);
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
}
