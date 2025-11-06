package co.edu.uco.api_gateway.dto;

public record ConfirmVerificationCodeResponse(boolean confirmed, String message) {
}
