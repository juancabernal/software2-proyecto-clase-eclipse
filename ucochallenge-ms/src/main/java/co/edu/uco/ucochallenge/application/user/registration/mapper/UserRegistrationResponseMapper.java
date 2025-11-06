package co.edu.uco.ucochallenge.application.user.registration.mapper;

import org.springframework.stereotype.Component;

import co.edu.uco.ucochallenge.application.interactor.mapper.DomainMapper;
import co.edu.uco.ucochallenge.application.user.registration.dto.UserRegistrationResponseDTO;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.domain.user.registration.model.UserRegistrationDomainModel;

@Component
public class UserRegistrationResponseMapper
                implements DomainMapper<UserRegistrationResponseDTO, UserRegistrationDomainModel> {

        @Override
        public UserRegistrationDomainModel toDomain(final UserRegistrationResponseDTO dto) {
                throw new UnsupportedOperationException("Conversion not supported");
        }

        @Override
        public UserRegistrationResponseDTO toDto(final UserRegistrationDomainModel domain) {
                return new UserRegistrationResponseDTO(
                                domain.getId(),
                                domain.getIdType(),
                                domain.getIdNumber(),
                                domain.getFirstName(),
                                TextHelper.isEmpty(domain.getSecondName()) ? null : domain.getSecondName(),
                                domain.getFirstSurname(),
                                TextHelper.isEmpty(domain.getSecondSurname()) ? null : domain.getSecondSurname(),
                                domain.hasEmail() ? domain.getEmail() : null,
                                domain.hasMobileNumber() ? domain.getMobileNumber() : null,
                                domain.getHomeCity());
        }
}
