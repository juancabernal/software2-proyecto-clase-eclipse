package co.edu.uco.ucochallenge.application.pagination.dto;

import co.edu.uco.ucochallenge.domain.pagination.PaginatedResult;

public record PaginationMetadataDTO(
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious) {

    public static PaginationMetadataDTO from(final PaginatedResult<?> result) {
        return new PaginationMetadataDTO(
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext(),
                result.hasPrevious());
    }
}
