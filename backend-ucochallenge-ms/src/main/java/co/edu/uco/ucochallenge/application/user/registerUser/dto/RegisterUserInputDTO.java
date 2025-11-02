package co.edu.uco.ucochallenge.application.user.registerUser.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;

import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.UUIDHelper;

public record RegisterUserInputDTO(
                @JsonAlias({ "idType", "tipoIdentificacion" }) UUID idType,
                @JsonAlias({ "idNumber", "numeroIdentificacion" }) String idNumber,
                @JsonAlias({ "firstName", "primerNombre" }) String firstName,
                @JsonAlias({ "secondName", "segundoNombre" }) String secondName,
                @JsonAlias({ "firstSurname", "primerApellido" }) String firstSurname,
                @JsonAlias({ "secondSurname", "segundoApellido" }) String secondSurname,
                @JsonAlias({ "homeCity", "ciudad" }) UUID homeCity,
                @JsonAlias({ "email", "correo" }) String email,
                @JsonAlias({ "mobileNumber", "telefono" }) String mobileNumber) {

        public static RegisterUserInputDTO normalize(
                        final UUID idType,
                        final String idNumber,
                        final String firstName,
                        final String secondName,
                        final String firstSurname,
                        final String secondSurname,
                        final UUID homeCity,
                        final String email,
                        final String mobileNumber) {
                final UUID idTypeNormalized = UUIDHelper.getDefault(idType);
                final String idNumberNormalized = TextHelper.getDefaultWithTrim(idNumber);
                final String firstNameNormalized = TextHelper.getDefaultWithTrim(firstName);
                final String secondNameNormalized = TextHelper.getDefaultWithTrim(secondName);
                final String firstSurnameNormalized = TextHelper.getDefaultWithTrim(firstSurname);
                final String secondSurnameNormalized = TextHelper.getDefaultWithTrim(secondSurname);
                final UUID homeCityNormalized = UUIDHelper.getDefault(homeCity);
                final String emailNormalized = TextHelper.getDefaultWithTrim(email);
                final String mobileNumberNormalized = TextHelper.getDefaultWithTrim(mobileNumber);

                return new RegisterUserInputDTO(
                                idTypeNormalized,
                                idNumberNormalized,
                                firstNameNormalized,
                                secondNameNormalized,
                                firstSurnameNormalized,
                                secondSurnameNormalized,
                                homeCityNormalized,
                                emailNormalized,
                                mobileNumberNormalized);
        }

        public static RegisterUserInputDTO normalize(final RegisterUserInputDTO dto) {
                if (dto == null) {
                        return new RegisterUserInputDTO(
                                        UUIDHelper.getDefault(),
                                        TextHelper.getDefault(),
                                        TextHelper.getDefault(),
                                        TextHelper.getDefault(),
                                        TextHelper.getDefault(),
                                        TextHelper.getDefault(),
                                        UUIDHelper.getDefault(),
                                        TextHelper.getDefault(),
                                        TextHelper.getDefault());
                }

                return normalize(
                                dto.idType(),
                                dto.idNumber(),
                                dto.firstName(),
                                dto.secondName(),
                                dto.firstSurname(),
                                dto.secondSurname(),
                                dto.homeCity(),
                                dto.email(),
                                dto.mobileNumber());
        }
}
