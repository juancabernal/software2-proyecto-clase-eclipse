package co.edu.uco.ucochallenge.application.pagination.dto;

import co.edu.uco.ucochallenge.domain.pagination.PageCriteria;

public record PaginationRequestDTO(int page, int size) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 2;
    private static final int MAX_SIZE = 100;

    public static PaginationRequestDTO normalize(final Integer page, final Integer size) {
        final int safePage = page == null || page < 0 ? DEFAULT_PAGE : page;
        final int safeSizeCandidate = size == null ? DEFAULT_SIZE : size;
        final int safeSize = Math.min(Math.max(safeSizeCandidate, 1), MAX_SIZE);
        return new PaginationRequestDTO(safePage, safeSize);
    }

    public PageCriteria toDomain() {
        return PageCriteria.of(page, size);
    }
}
