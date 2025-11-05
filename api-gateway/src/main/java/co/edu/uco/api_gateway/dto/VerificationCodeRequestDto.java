package co.edu.uco.api_gateway.dto;

import java.util.UUID;

public record VerificationCodeRequestDto(UUID tokenId, String code) {
}
