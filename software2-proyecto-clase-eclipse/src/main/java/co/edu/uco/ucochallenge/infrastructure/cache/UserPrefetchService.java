package co.edu.uco.ucochallenge.infrastructure.cache;

import java.util.concurrent.CompletableFuture;

import co.edu.uco.ucochallenge.application.pagination.dto.PaginationRequestDTO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import co.edu.uco.ucochallenge.application.user.listUsers.dto.*;
import co.edu.uco.ucochallenge.application.user.listUsers.usecase.ListUsersUseCase;

@Service
public class UserPrefetchService {

    private final ListUsersUseCase useCase;

    public UserPrefetchService(ListUsersUseCase useCase) {
        this.useCase = useCase;
    }

    @Async
    public CompletableFuture<Void> prefetchNextPages(int startPage, int size, int totalToCache) {
        int totalPages = (int) Math.ceil(totalToCache / (double) size);
        for (int p = startPage; p < startPage + totalPages; p++) {
            PaginationRequestDTO pagination = PaginationRequestDTO.normalize(p, size);
            useCase.execute(pagination.toDomain());
            // gracias a @Cacheable, cada llamada llenará el caché automáticamente
        }
        return CompletableFuture.completedFuture(null);
    }
}
