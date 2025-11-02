package co.edu.uco.api_gateway.dto;

public record UserCreateRequest(
        String idType,
        String idNumber,
        String firstName,
        String secondName,
        String firstSurname,
        String secondSurname,
        String homeCity,
        String email,
        String mobileNumber) {
}
