package co.edu.uco.ucochallenge.application.pagination.dto;

import co.edu.uco.ucochallenge.domain.pagination.PageCriteria;
import java.util.Set;

public record PaginationRequestDTO(int page, int size) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final Set<Integer> ALLOWED_SIZES = Set.of(5, 10, 20, 50);

    public static PaginationRequestDTO normalize(final Integer page, final Integer size) {
        // Si el front manda null o negativo, usar la primera página (0)
        final int safePage = (page == null || page < 0) ? DEFAULT_PAGE : page;

        // Validar el tamaño permitido
        int safeSize = (size == null) ? DEFAULT_SIZE : size;
        if (!ALLOWED_SIZES.contains(safeSize)) {
            safeSize = DEFAULT_SIZE;
        }

        return new PaginationRequestDTO(safePage, safeSize);
    }

    public PageCriteria toDomain() {
        return PageCriteria.of(page, size);
    }
}
