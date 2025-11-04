package co.edu.uco.ucochallenge.application.user.searchUsers.interactor.impl;

import co.edu.uco.ucochallenge.application.user.listUsers.dto.ListUsersResponseDTO;
import co.edu.uco.ucochallenge.application.user.listUsers.mapper.ListUsersMapper;
import co.edu.uco.ucochallenge.application.user.searchUsers.CacheKeyUtil;
import co.edu.uco.ucochallenge.application.user.searchUsers.dto.SearchUsersQueryDTO;
import co.edu.uco.ucochallenge.application.user.searchUsers.interactor.SearchUsersInteractor;
import co.edu.uco.ucochallenge.application.user.searchUsers.usecase.SearchUsersUseCase;
import co.edu.uco.ucochallenge.infrastructure.cache.SearchUsersPrefetchService;
import co.edu.uco.ucochallenge.infrastructure.cache.UserPrefetchService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SearchUsersInteractorImpl implements SearchUsersInteractor {
        private final SearchUsersUseCase useCase;
        private final ListUsersMapper mapper;
        private final SearchUsersPrefetchService prefetchService; // final

        public SearchUsersInteractorImpl(
                final SearchUsersUseCase useCase,
                final ListUsersMapper mapper,
                final SearchUsersPrefetchService prefetchService // ⬅️ injéctalo
        ) {
                this.useCase = useCase;
                this.mapper = mapper;
                this.prefetchService = prefetchService;
        }

        @Override
        @Cacheable(
                value = "users.search",
                condition = "@cacheFlags.enabled()", // ⬅️ si app.cache.enabled=false, NO usa caché
                key = "T(co.edu.uco.ucochallenge.application.user.searchUsers.CacheKeyUtil)"
                        + ".canonicalKey("
                        + "#dto.filter().idType(), #dto.filter().idNumber(), #dto.filter().firstName(), "
                        + "#dto.filter().firstSurname(), #dto.filter().homeCity(), #dto.filter().email(), "
                        + "#dto.filter().mobileNumber(), #dto.filter().q(), " // ⬅️ ver sección 2
                        + "#dto.pagination().page(), #dto.pagination().size())",
                unless = "#result == null || #result.users() == null || #result.users().isEmpty()"
        )
        public ListUsersResponseDTO execute(final SearchUsersQueryDTO dto) {
                var normalized = SearchUsersQueryDTO.normalize(dto);
                var response = mapper.toResponse(useCase.execute(normalized.toDomain()));

                if (normalized.pagination().page() == 0
                        && response != null
                        && response.pagination() != null
                        && response.pagination().totalElements() > normalized.pagination().size()) {
                        try {
                                prefetchService.prefetchNextPages(normalized, 1, normalized.pagination().size());
                        } catch (Exception ignore) { /* nunca tumbar la request por prefetch */ }
                }
                return response;
        }
}

