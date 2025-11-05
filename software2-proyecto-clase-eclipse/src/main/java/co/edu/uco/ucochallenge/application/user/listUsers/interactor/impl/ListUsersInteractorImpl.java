package co.edu.uco.ucochallenge.application.user.listUsers.interactor.impl;

import co.edu.uco.ucochallenge.infrastructure.cache.UserPrefetchService;
import co.edu.uco.ucochallenge.infrastructure.secondary.ports.service.CachedListUsersPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.edu.uco.ucochallenge.application.pagination.dto.PaginationRequestDTO;
import co.edu.uco.ucochallenge.application.user.listUsers.dto.ListUsersRequestDTO;
import co.edu.uco.ucochallenge.application.user.listUsers.dto.ListUsersResponseDTO;
import co.edu.uco.ucochallenge.application.user.listUsers.interactor.ListUsersInteractor;


@Service
@Transactional(readOnly = true)
public class ListUsersInteractorImpl implements ListUsersInteractor {
        private static final Logger LOGGER = LoggerFactory.getLogger(ListUsersInteractorImpl.class);

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

                if (pagination.page() == 0) {
                        try {
                                prefetchService.prefetchNextPages(1, pagination.size(), 100);
                        } catch (Exception exception) {
                                LOGGER.warn("Failed to prefetch user list pages", exception);
                        }
                }
                return response;
        }
}

