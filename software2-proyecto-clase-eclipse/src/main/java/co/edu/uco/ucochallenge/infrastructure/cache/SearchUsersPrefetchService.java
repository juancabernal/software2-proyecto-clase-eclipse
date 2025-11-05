package co.edu.uco.ucochallenge.infrastructure.cache;


import co.edu.uco.ucochallenge.application.user.searchUsers.dto.SearchUsersQueryDTO;
import co.edu.uco.ucochallenge.application.user.searchUsers.usecase.SearchUsersUseCase;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.CompletableFuture;

@Service
public class SearchUsersPrefetchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchUsersPrefetchService.class);
    private final SearchUsersUseCase useCase;
    private final CacheFlags cacheFlags;

    private static final int TOTAL_TO_CACHE = 100;

    public SearchUsersPrefetchService(SearchUsersUseCase useCase, CacheFlags cacheFlags) {
        this.useCase = useCase;
        this.cacheFlags = cacheFlags;
    }

    @Async
    public CompletableFuture<Void> prefetchNextPages(SearchUsersQueryDTO normalized, int startPage, int size) {
        if (!cacheFlags.enabled() || normalized == null || size <= 0) {
            return CompletableFuture.completedFuture(null);
        }

        final int safeSize = Math.max(size, 1);
        final int pages = (int) Math.ceil(TOTAL_TO_CACHE / (double) safeSize);
        try {
            for (int p = startPage; p < startPage + pages; p++) {
                var nextQuery = normalized.withPagination(p, safeSize);
                useCase.execute(nextQuery.toDomain());
                // Las llamadas llenan el caché gracias a @Cacheable en el interactor
            }
        } catch (Exception exception) {
            LOGGER.warn("Search users prefetch failed starting at page {}", startPage, exception);
        }
        return CompletableFuture.completedFuture(null);
    }
}
