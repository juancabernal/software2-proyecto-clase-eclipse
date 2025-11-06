package co.edu.uco.api_gateway.dto;

import java.util.UUID;

/**
 * Payload returned when requesting an email confirmation code. It mirrors the structure
 * exposed by the UcoChallenge backend so the administrative frontend can retrieve the
 * verification identifier associated with the generated token.
 */
public record EmailConfirmationResponse(UUID verificationId) {
}

