package co.edu.uco.ucochallenge.application.notification;

import co.edu.uco.ucochallenge.domain.verification.model.VerificationToken;

import java.util.UUID; // ✅ FIX: Keep UUID dependency for verification identifiers


public record ConfirmationResponseDTO(
        UUID verificationId,
        String contact,
        String channel,
        int remainingSeconds
) {
    public static ConfirmationResponseDTO from(VerificationToken token, String contact, VerificationChannel channel, int ttlSeconds) {

        return new ConfirmationResponseDTO(token.id(), contact, channel.name(), ttlSeconds);
    }
}

