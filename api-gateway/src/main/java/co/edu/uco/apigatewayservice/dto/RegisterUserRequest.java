package co.edu.uco.apigatewayservice.dto;

import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterUserRequest(
        UUID idTypeId,
        String idTypeName,
        @NotBlank(message = "register.user.validation.idnumber.required") String idNumber,
        @NotBlank(message = "register.user.validation.firstname.required") String firstName,
        String middleName,
        @NotBlank(message = "register.user.validation.lastname.required") String lastName,
        String secondLastName,
        @Email(message = "register.user.validation.email.invalid") String email,
        String mobile,
        @NotNull(message = "register.user.validation.country.required") UUID countryId,
        @NotNull(message = "register.user.validation.department.required") UUID departmentId,
        @NotNull(message = "register.user.validation.city.required") UUID cityId) {
}
