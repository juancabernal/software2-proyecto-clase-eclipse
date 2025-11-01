package co.edu.uco.ucochallenge.application.user.searchUsers.interactor.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uco.ucochallenge.application.user.listUsers.dto.ListUsersResponseDTO;
import co.edu.uco.ucochallenge.application.user.listUsers.mapper.ListUsersMapper;
import co.edu.uco.ucochallenge.application.user.searchUsers.dto.SearchUsersQueryDTO;
import co.edu.uco.ucochallenge.application.user.searchUsers.interactor.SearchUsersInteractor;
import co.edu.uco.ucochallenge.application.user.searchUsers.usecase.SearchUsersUseCase;

@Service
@Transactional(readOnly = true)
public class SearchUsersInteractorImpl implements SearchUsersInteractor {

        private final SearchUsersUseCase useCase;
        private final ListUsersMapper mapper;

        public SearchUsersInteractorImpl(final SearchUsersUseCase useCase, final ListUsersMapper mapper) {
                this.useCase = useCase;
                this.mapper = mapper;
        }

        @Override
        public ListUsersResponseDTO execute(final SearchUsersQueryDTO dto) {
                final SearchUsersQueryDTO normalized = SearchUsersQueryDTO.normalize(dto);
                return mapper.toResponse(useCase.execute(normalized.toDomain()));
        }
}
