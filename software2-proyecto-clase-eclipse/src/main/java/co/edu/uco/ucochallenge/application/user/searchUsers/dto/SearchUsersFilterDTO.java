package co.edu.uco.ucochallenge.application.user.searchUsers.dto;

import java.util.UUID;

import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.UUIDHelper;
import co.edu.uco.ucochallenge.domain.user.model.UserFilter;

public record SearchUsersFilterDTO(
                UUID idType,
                String idNumber,
                String firstName,
                String firstSurname,
                UUID homeCity,
                String email,
                String mobileNumber) {

        public static SearchUsersFilterDTO normalize(
                        final UUID idType,
                        final String idNumber,
                        final String firstName,
                        final String firstSurname,
                        final UUID homeCity,
                        final String email,
                        final String mobileNumber) {
                return new SearchUsersFilterDTO(
                                UUIDHelper.getDefault(idType),
                                TextHelper.getDefaultWithTrim(idNumber),
                                TextHelper.getDefaultWithTrim(firstName),
                                TextHelper.getDefaultWithTrim(firstSurname),
                                UUIDHelper.getDefault(homeCity),
                                TextHelper.getDefaultWithTrim(email),
                                TextHelper.getDefaultWithTrim(mobileNumber));
        }

        public static SearchUsersFilterDTO normalize(final SearchUsersFilterDTO dto) {
                if (dto == null) {
                        return normalize(
                                        UUIDHelper.getDefault(),
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
                                dto.firstSurname(),
                                dto.homeCity(),
                                dto.email(),
                                dto.mobileNumber());
        }

        public UserFilter toDomain() {
                return new UserFilter(
                                idType,
                                idNumber,
                                firstName,
                                firstSurname,
                                homeCity,
                                email,
                                mobileNumber);
        }
}
