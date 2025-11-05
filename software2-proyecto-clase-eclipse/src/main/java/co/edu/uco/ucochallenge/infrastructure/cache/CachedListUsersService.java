package co.edu.uco.ucochallenge.infrastructure.cache;

import co.edu.uco.ucochallenge.infrastructure.secondary.ports.service.CachedListUsersPort;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uco.ucochallenge.application.pagination.dto.PaginationRequestDTO;
import co.edu.uco.ucochallenge.application.user.listUsers.dto.ListUsersResponseDTO;
import co.edu.uco.ucochallenge.application.user.listUsers.mapper.ListUsersMapper;
import co.edu.uco.ucochallenge.application.user.listUsers.usecase.ListUsersUseCase;

@Service
@Transactional(readOnly = true)
public class CachedListUsersService implements CachedListUsersPort {

    private final ListUsersUseCase useCase;
    private final ListUsersMapper mapper;

    public CachedListUsersService(ListUsersUseCase useCase, ListUsersMapper mapper) {
        this.useCase = useCase;
        this.mapper = mapper;
    }

    @Override
    @Cacheable(
            value = "users.pages",
            condition = "@cacheFlags.enabled()",
            key = "'page=' + #pagination.page() + ',size=' + #pagination.size()",
            unless = "#result == null || #result.users().isEmpty()"
    )
    public ListUsersResponseDTO getPage(PaginationRequestDTO pagination) {
        return mapper.toResponse(useCase.execute(pagination.toDomain()));
    }
}
