package co.edu.uco.apigatewayservice.dto;

import java.util.UUID;

public record RegisterUserResponse(
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
