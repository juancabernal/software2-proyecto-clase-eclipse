package co.edu.uco.api_gateway.dto;

public record ConfirmationResponseDto(int remainingSeconds, String tokenId) {
}
