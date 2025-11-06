package co.edu.uco.ucochallenge.application.user.registration.interactor;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.interactor.mapper.DomainMapper;
import co.edu.uco.ucochallenge.application.user.registration.dto.UserRegistrationRequestDTO;
import co.edu.uco.ucochallenge.application.user.registration.dto.UserRegistrationResponseDTO;
import co.edu.uco.ucochallenge.application.user.registration.usecase.UserRegistrationCommandUseCase;
import co.edu.uco.ucochallenge.domain.user.registration.model.UserRegistrationDomainModel;
import jakarta.transaction.Transactional;

@Transactional
@Service
public class UserRegistrationApplicationInteractorImpl implements UserRegistrationApplicationInteractor {

        private final UserRegistrationCommandUseCase useCase;
        private final DomainMapper<UserRegistrationRequestDTO, UserRegistrationDomainModel> requestMapper;
        private final DomainMapper<UserRegistrationResponseDTO, UserRegistrationDomainModel> responseMapper;

        public UserRegistrationApplicationInteractorImpl(final UserRegistrationCommandUseCase useCase,
                        final DomainMapper<UserRegistrationRequestDTO, UserRegistrationDomainModel> requestMapper,
                        final DomainMapper<UserRegistrationResponseDTO, UserRegistrationDomainModel> responseMapper) {
                this.useCase = useCase;
                this.requestMapper = requestMapper;
                this.responseMapper = responseMapper;
        }

        @Override
        public UserRegistrationResponseDTO execute(final UserRegistrationRequestDTO dto) {
                final var domain = requestMapper.toDomain(dto);
                final var result = useCase.execute(domain);
                return responseMapper.toDto(result);
        }
}
