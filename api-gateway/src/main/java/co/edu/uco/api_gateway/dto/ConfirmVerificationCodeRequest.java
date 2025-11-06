package co.edu.uco.api_gateway.dto;

public record ConfirmVerificationCodeRequest(String channel, String code) {
}
