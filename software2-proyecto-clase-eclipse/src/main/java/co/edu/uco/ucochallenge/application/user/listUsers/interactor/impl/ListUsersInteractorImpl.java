package co.edu.uco.ucochallenge.application.user.listUsers.interactor.impl;

import co.edu.uco.ucochallenge.application.pagination.dto.PaginationRequestDTO;
import co.edu.uco.ucochallenge.application.user.listUsers.dto.ListUsersRequestDTO;
import co.edu.uco.ucochallenge.application.user.listUsers.dto.ListUsersResponseDTO;
import co.edu.uco.ucochallenge.application.user.listUsers.interactor.ListUsersInteractor;
import co.edu.uco.ucochallenge.application.user.listUsers.mapper.ListUsersMapper;
import co.edu.uco.ucochallenge.application.user.listUsers.usecase.ListUsersUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListUsersInteractorImpl implements ListUsersInteractor {

        private final ListUsersUseCase useCase;
        private final ListUsersMapper mapper;

        public ListUsersInteractorImpl(final ListUsersUseCase useCase, final ListUsersMapper mapper) {
                this.useCase = useCase;
                this.mapper = mapper;
        }

        @Override
        public ListUsersResponseDTO execute(final ListUsersRequestDTO dto) {
                final PaginationRequestDTO pagination = dto == null || dto.pagination() == null
                                ? PaginationRequestDTO.normalize(null, null)
                                : dto.pagination();
                return mapper.toResponse(useCase.execute(pagination.toDomain()));
        }
}
