package co.edu.uco.api_gateway.dto;

import java.util.UUID;

public record RegisterUserResponse(UUID userId, String fullName, String email) {
}
