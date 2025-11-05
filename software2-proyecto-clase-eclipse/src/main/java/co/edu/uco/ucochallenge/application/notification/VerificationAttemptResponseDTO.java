package co.edu.uco.ucochallenge.application.notification;

public record VerificationAttemptResponseDTO(
        boolean success,
        boolean expired,
        int attemptsRemaining,
        boolean contactConfirmed,
        boolean allContactsConfirmed,
        String message) {
}