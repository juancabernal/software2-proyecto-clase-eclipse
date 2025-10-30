package co.edu.uco.ucochallenge.application.user.registerUser.dto;

import java.util.UUID;

public record RegisterUserOutputDTO(
                UUID userId,
                String fullName,
                String email) {
}
