package co.edu.uco.api_gateway.dto;

public record VerificationCodeRequest(
        String token,
        String code,
        String verifiedAt) {
}
