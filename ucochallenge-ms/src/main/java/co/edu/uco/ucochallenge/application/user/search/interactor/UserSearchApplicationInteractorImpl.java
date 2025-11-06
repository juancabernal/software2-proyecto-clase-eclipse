package co.edu.uco.ucochallenge.application.user.search.interactor;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.interactor.mapper.DomainMapper;
import co.edu.uco.ucochallenge.application.user.search.dto.UserSearchQueryRequestDTO;
import co.edu.uco.ucochallenge.application.user.search.dto.UserSearchQueryResponseDTO;
import co.edu.uco.ucochallenge.application.user.search.usecase.UserSearchQueryUseCase;
import co.edu.uco.ucochallenge.domain.user.search.model.UserSearchFilterDomainModel;
import co.edu.uco.ucochallenge.domain.user.search.model.UserSearchResultDomainModel;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserSearchApplicationInteractorImpl implements UserSearchApplicationInteractor {

        private final UserSearchQueryUseCase useCase;
        private final DomainMapper<UserSearchQueryRequestDTO, UserSearchFilterDomainModel> requestMapper;
        private final DomainMapper<UserSearchQueryResponseDTO, UserSearchResultDomainModel> responseMapper;

        public UserSearchApplicationInteractorImpl(final UserSearchQueryUseCase useCase,
                        final DomainMapper<UserSearchQueryRequestDTO, UserSearchFilterDomainModel> requestMapper,
                        final DomainMapper<UserSearchQueryResponseDTO, UserSearchResultDomainModel> responseMapper) {
                this.useCase = useCase;
                this.requestMapper = requestMapper;
                this.responseMapper = responseMapper;
        }

        @Override
        public UserSearchQueryResponseDTO execute(final UserSearchQueryRequestDTO dto) {
                final var domain = requestMapper.toDomain(dto);
                final var result = useCase.execute(domain);
                return responseMapper.toDto(result);
        }
}
