package co.edu.uco.ucochallenge.application.user.contactvalidation.dto;

import java.util.UUID;

import co.edu.uco.ucochallenge.application.notification.ConfirmationResponseDTO;

/**
 * Response payload for email confirmation requests, exposing the verification token identifier
 * generated during the process.
 */
public record EmailConfirmationResponseDTO(UUID verificationId) {

    public static EmailConfirmationResponseDTO from(final ConfirmationResponseDTO response) {
        return new EmailConfirmationResponseDTO(response.verificationId());
    }
}

