package co.edu.uco.api_gateway.dto;

import java.util.UUID;

/**
 * Representa la respuesta detallada de un usuario retornada por el backend
 * <strong>uco-challenge</strong> al consultar un usuario por su identificador.
 */
public record GetUserResponse(
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
