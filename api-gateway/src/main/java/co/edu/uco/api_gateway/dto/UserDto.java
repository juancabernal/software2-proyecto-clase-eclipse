package co.edu.uco.api_gateway.dto;

import java.util.UUID;

public record UserDto(
        UUID id,
        String primerNombre,
        String segundoNombre,
        String primerApellido,
        String segundoApellido,
        String correo,
        String telefono,
        String ciudad,
        String estado,
        String pais) {
}
