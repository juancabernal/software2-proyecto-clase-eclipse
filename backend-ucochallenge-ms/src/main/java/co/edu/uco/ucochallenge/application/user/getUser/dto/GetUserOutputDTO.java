package co.edu.uco.ucochallenge.application.user.getUser.dto;

import java.util.UUID;

public record GetUserOutputDTO(
                UUID userId,
                UUID idType,
                String idNumber,
                String firstName,
                String secondName,
                String firstSurname,
                String secondSurname,
                UUID homeCity,
                String email,
                String mobileNumber,
                boolean emailConfirmed,
                boolean mobileNumberConfirmed) {
}
