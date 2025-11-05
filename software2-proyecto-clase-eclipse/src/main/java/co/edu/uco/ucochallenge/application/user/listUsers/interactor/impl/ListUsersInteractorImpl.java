package co.edu.uco.ucochallenge.application.user.listUsers.interactor.impl;

import co.edu.uco.ucochallenge.infrastructure.cache.UserPrefetchService;
import co.edu.uco.ucochallenge.infrastructure.secondary.ports.service.CachedListUsersPort;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uco.ucochallenge.application.pagination.dto.PaginationRequestDTO;
import co.edu.uco.ucochallenge.application.user.listUsers.dto.ListUsersRequestDTO;
import co.edu.uco.ucochallenge.application.user.listUsers.dto.ListUsersResponseDTO;
import co.edu.uco.ucochallenge.application.user.listUsers.interactor.ListUsersInteractor;
import co.edu.uco.ucochallenge.application.user.listUsers.mapper.ListUsersMapper;
import co.edu.uco.ucochallenge.application.user.listUsers.usecase.ListUsersUseCase;

@Service
@Transactional(readOnly = true)
public class ListUsersInteractorImpl implements ListUsersInteractor {

        private final CachedListUsersPort cachedListUsersPort;
        private final UserPrefetchService prefetchService;

        public ListUsersInteractorImpl(CachedListUsersPort cachedListUsersPort, UserPrefetchService prefetchService) {
                this.cachedListUsersPort = cachedListUsersPort;
                this.prefetchService = prefetchService;
        }

        @Override
        public ListUsersResponseDTO execute(final ListUsersRequestDTO dto) {
                var pagination = dto == null || dto.pagination() == null
                        ? PaginationRequestDTO.normalize(null, null)
                        : dto.pagination();

                var response = cachedListUsersPort.getPage(pagination);

                if (pagination.page() == 1) {
                        prefetchService.prefetchNextPages(2, pagination.size(), 100);
                }
                return response;
        }
}

