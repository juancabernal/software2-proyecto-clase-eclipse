package co.edu.uco.ucochallenge.application.user.contactvalidation.dto;

/**
 * Response returned when a verification code is processed.
 */
public record ConfirmVerificationCodeResponseDTO(boolean confirmed, String message) {
}
