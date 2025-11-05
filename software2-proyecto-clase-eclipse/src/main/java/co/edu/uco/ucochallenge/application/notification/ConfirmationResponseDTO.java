package co.edu.uco.ucochallenge.application.notification;

import java.util.UUID;

public record ConfirmationResponseDTO(int remainingSeconds, UUID tokenId) {
}

