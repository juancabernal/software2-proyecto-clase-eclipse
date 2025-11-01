package co.edu.uco.ucochallenge.domain.pagination;

import java.util.Collections;
import java.util.List;

import co.edu.uco.ucochallenge.crosscuting.helper.ObjectHelper;

public final class PaginatedResult<T> {

    private final List<T> items;
    private final long totalElements;
    private final int totalPages;
    private final int page;
    private final int size;

    private PaginatedResult(
            final List<T> items,
            final long totalElements,
            final int totalPages,
            final int page,
            final int size) {
        this.items = Collections.unmodifiableList(ObjectHelper.getDefault(items, List.of()));
        this.totalElements = Math.max(totalElements, 0);
        this.totalPages = Math.max(totalPages, 0);
        this.page = Math.max(page, 0);
        this.size = Math.max(size, 0);
    }

    public static <T> PaginatedResult<T> of(
            final List<T> items,
            final long totalElements,
            final int totalPages,
            final int page,
            final int size) {
        return new PaginatedResult<>(items, totalElements, totalPages, page, size);
    }

    public List<T> items() {
        return items;
    }

    public long totalElements() {
        return totalElements;
    }

    public int totalPages() {
        return totalPages;
    }

    public int page() {
        return page;
    }

    public int size() {
        return size;
    }

    public boolean hasNext() {
        return page + 1 < totalPages;
    }

    public boolean hasPrevious() {
        return page > 0 && totalPages > 0;
    }
}
