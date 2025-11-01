package co.edu.uco.ucochallenge.application.user.updateUser.dto;

import java.util.UUID;

import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.UUIDHelper;

public record UpdateUserInputDTO(
                UUID idType,
                String idNumber,
                String firstName,
                String secondName,
                String firstSurname,
                String secondSurname,
                UUID homeCity,
                String email,
                String mobileNumber) {

        public static UpdateUserInputDTO normalize(final UpdateUserInputDTO dto) {
                if (dto == null) {
                        return new UpdateUserInputDTO(
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

                return new UpdateUserInputDTO(
                                UUIDHelper.getDefault(dto.idType()),
                                TextHelper.getDefaultWithTrim(dto.idNumber()),
                                TextHelper.getDefaultWithTrim(dto.firstName()),
                                TextHelper.getDefaultWithTrim(dto.secondName()),
                                TextHelper.getDefaultWithTrim(dto.firstSurname()),
                                TextHelper.getDefaultWithTrim(dto.secondSurname()),
                                UUIDHelper.getDefault(dto.homeCity()),
                                TextHelper.getDefaultWithTrim(dto.email()),
                                TextHelper.getDefaultWithTrim(dto.mobileNumber()));
        }
}
