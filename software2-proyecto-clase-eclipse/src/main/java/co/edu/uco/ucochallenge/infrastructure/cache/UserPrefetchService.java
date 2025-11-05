package co.edu.uco.ucochallenge.infrastructure.cache;

import java.util.concurrent.CompletableFuture;

import co.edu.uco.ucochallenge.application.pagination.dto.PaginationRequestDTO;
import co.edu.uco.ucochallenge.application.user.listUsers.usecase.ListUsersUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import co.edu.uco.ucochallenge.application.user.listUsers.dto.*;
import co.edu.uco.ucochallenge.application.user.listUsers.usecase.ListUsersUseCase;

@Service
public class UserPrefetchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserPrefetchService.class);
    private final ListUsersUseCase useCase;
    private final CacheFlags cacheFlags;
    public UserPrefetchService(ListUsersUseCase useCase, CacheFlags cacheFlags) {
        this.useCase = useCase;
        this.cacheFlags = cacheFlags;
    }

    @Async
    public CompletableFuture<Void> prefetchNextPages(int startPage, int size, int totalToCache) {
        if (!cacheFlags.enabled() || size <= 0 || totalToCache <= 0) {
            return CompletableFuture.completedFuture(null);
        }

        final int safeSize = Math.max(size, 1);
        final int totalPages = (int) Math.ceil(totalToCache / (double) safeSize);
        try {
            for (int p = startPage; p < startPage + totalPages; p++) {
                PaginationRequestDTO pagination = PaginationRequestDTO.normalize(p, safeSize);
                useCase.execute(pagination.toDomain());
                // gracias a @Cacheable, cada llamada llenará el caché automáticamente
            }
        } catch (Exception exception) {
            LOGGER.warn("User list prefetch failed starting at page {}", startPage, exception);
        }
        return CompletableFuture.completedFuture(null);
    }
}
