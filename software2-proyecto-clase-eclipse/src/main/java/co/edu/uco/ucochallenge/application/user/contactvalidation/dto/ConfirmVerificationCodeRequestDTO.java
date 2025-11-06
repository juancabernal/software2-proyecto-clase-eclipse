package co.edu.uco.ucochallenge.application.user.contactvalidation.dto;

import co.edu.uco.ucochallenge.application.notification.VerificationChannel;
import co.edu.uco.ucochallenge.crosscuting.exception.DomainException;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageCodes;

/**
 * Request payload used when a verification code is confirmed through the REST API.
 */
public record ConfirmVerificationCodeRequestDTO(String channel, String code) {

    private static final String CODE_PATTERN = "\\d{6}";

    public VerificationChannel sanitizedChannel() {
        return VerificationChannel.from(channel);
    }

    public String sanitizedCode() {
        final String sanitized = TextHelper.getDefaultWithTrim(code);
        if (TextHelper.isEmpty(sanitized) || !sanitized.matches(CODE_PATTERN)) {
            throw DomainException.buildFromCatalog(
                    MessageCodes.Domain.Verification.TOKEN_INVALID_TECHNICAL,
                    MessageCodes.Domain.Verification.TOKEN_INVALID_USER);
        }
        return sanitized;
    }
}
