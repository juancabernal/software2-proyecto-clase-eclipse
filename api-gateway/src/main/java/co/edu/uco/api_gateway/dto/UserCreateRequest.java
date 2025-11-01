package co.edu.uco.api_gateway.dto;

public record UserCreateRequest(
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
