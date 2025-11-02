package co.edu.uco.ucochallenge.infrastructure.secondary.adapters.service.dto;

public record RemoteNotification(
        String key,
        String channel,
        String subject,
        String body) {
}