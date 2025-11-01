package co.edu.uco.ucochallenge.application.user.listUsers.dto;

import java.util.UUID;

public record ListUsersOutputDTO(
                UUID userId,
                String idType,
                String idNumber,
                String fullName,
                String email,
                String mobileNumber,
                boolean emailConfirmed,
                boolean mobileNumberConfirmed) {
}
