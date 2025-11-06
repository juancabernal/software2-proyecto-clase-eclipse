package co.edu.uco.ucochallenge.application.user.registration.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import co.edu.uco.ucochallenge.application.interactor.mapper.DomainMapper;
import co.edu.uco.ucochallenge.application.user.registration.dto.UserRegistrationRequestDTO;
import co.edu.uco.ucochallenge.domain.user.registration.model.UserRegistrationDomainModel;

@Component
public class UserRegistrationRequestMapper
                implements DomainMapper<UserRegistrationRequestDTO, UserRegistrationDomainModel> {

        @Override
        public UserRegistrationDomainModel toDomain(final UserRegistrationRequestDTO dto) {
                return UserRegistrationDomainModel.builder()
                                .id(UUID.randomUUID())
                                .idType(dto.idTypeId())
                                .idTypeName(dto.idTypeName())
                                .idNumber(dto.idNumber())
                                .firstName(dto.firstName())
                                .secondName(dto.middleName())
                                .firstSurname(dto.lastName())
                                .secondSurname(dto.secondLastName())
                                .countryId(dto.countryId())
                                .departmentId(dto.departmentId())
                                .homeCity(dto.cityId())
                                .email(dto.email())
                                .mobileNumber(dto.mobile())
                                .build();
        }

        @Override
        public UserRegistrationRequestDTO toDto(final UserRegistrationDomainModel domain) {
                return new UserRegistrationRequestDTO(
                                domain.getIdType(),
                                domain.getIdTypeName(),
                                domain.getIdNumber(),
                                domain.getFirstName(),
                                domain.getSecondName(),
                                domain.getFirstSurname(),
                                domain.getSecondSurname(),
                                domain.getEmail(),
                                domain.getMobileNumber(),
                                domain.getCountryId(),
                                domain.getDepartmentId(),
                                domain.getHomeCity());
        }
}
