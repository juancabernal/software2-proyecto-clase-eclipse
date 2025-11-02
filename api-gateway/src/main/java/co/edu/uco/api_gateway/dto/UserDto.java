package co.edu.uco.api_gateway.dto;

import java.util.UUID;

public record UserDto(
        UUID userId,
        String idType,
        String idNumber,
        String fullName,
        String email,
        String mobileNumber,
        boolean emailConfirmed,
        boolean mobileNumberConfirmed) {
}
