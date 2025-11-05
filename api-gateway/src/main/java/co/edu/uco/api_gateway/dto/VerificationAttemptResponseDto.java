package co.edu.uco.api_gateway.dto;

public record VerificationAttemptResponseDto(
        boolean success,
        boolean expired,
        int attemptsRemaining,
        boolean contactConfirmed,
        boolean allContactsConfirmed,
        String message) {
}
