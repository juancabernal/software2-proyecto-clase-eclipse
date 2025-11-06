package co.edu.uco.api_gateway.dto;

import java.util.UUID;

/**
 * Response payload produced when a verification token is issued (email or mobile).
 * It contains the remaining time-to-live and the identifiers required to validate
 * the token from the administrative frontend.
 */
public record ConfirmationResponse(int remainingSeconds, UUID tokenId, UUID verificationId) {
}

