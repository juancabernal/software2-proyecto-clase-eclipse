package co.edu.uco.ucochallenge.application.user.registration.dto;

import java.util.UUID;

public record UserRegistrationResponseDTO(
                UUID id,
                UUID idTypeId,
                String idNumber,
                String firstName,
                String middleName,
                String lastName,
                String secondLastName,
                String email,
                String mobile,
                UUID cityId) {
}
