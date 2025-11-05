package co.edu.uco.ucochallenge.infrastructure.cache;

import co.edu.uco.ucochallenge.application.pagination.dto.PaginationRequestDTO;
import co.edu.uco.ucochallenge.application.user.searchUsers.dto.SearchUsersQueryDTO;
import co.edu.uco.ucochallenge.application.user.searchUsers.usecase.SearchUsersUseCase;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class SearchUsersPrefetchService {

    private final SearchUsersUseCase useCase;

    private static final int TOTAL_TO_CACHE = 100;

    public SearchUsersPrefetchService(SearchUsersUseCase useCase) {
        this.useCase = useCase;
    }

    @Async
    public CompletableFuture<Void> prefetchNextPages(SearchUsersQueryDTO normalized, int startPage, int size) {
        int pages = (int) Math.ceil(TOTAL_TO_CACHE / (double) size);
        for (int p = startPage; p < startPage + pages; p++) {
            var nextQuery = normalized.withPagination(p, size);
            useCase.execute(nextQuery.toDomain());
            // Las llamadas llenan el caché gracias a @Cacheable en el interactor
        }
        return CompletableFuture.completedFuture(null);
    }
}
