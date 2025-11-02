package co.edu.uco.api_gateway.dto;

public record PaginationMetadataDto(
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious) {
}
