package co.edu.uco.api_gateway.dto;

import java.util.UUID;

public record ConfirmationRequestResponseDto(int remainingSeconds, UUID tokenId) {
}
