package co.edu.uco.ucochallenge.application.user.contactvalidation.dto;

import java.util.UUID;

import co.edu.uco.ucochallenge.application.notification.ConfirmationResponseDTO;

/**
 * Response payload for email confirmation requests, exposing the verification token identifier
 * generated during the process.
 */
public record EmailConfirmationResponseDTO(
        UUID verificationId,
        String contact,
        String channel,
        int remainingSeconds
) {
    public static EmailConfirmationResponseDTO from(final ConfirmationResponseDTO source) {
        if (source == null) {
            return new EmailConfirmationResponseDTO(null, null, null, 0);
        }
        return new EmailConfirmationResponseDTO(
                source.verificationId(),   // ✅ debe coincidir con el record del servicio
                source.contact(),
                source.channel(),
                source.remainingSeconds()
        );
    }
}
