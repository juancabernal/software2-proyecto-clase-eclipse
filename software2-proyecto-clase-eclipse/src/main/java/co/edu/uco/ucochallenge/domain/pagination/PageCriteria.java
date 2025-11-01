package co.edu.uco.ucochallenge.domain.pagination;

import co.edu.uco.ucochallenge.crosscuting.exception.DomainException;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageCodes;

public record PageCriteria(int page, int size) {

    private static final int MIN_PAGE = 0;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 100;

    public PageCriteria {
        if (page < MIN_PAGE) {
            throw DomainException.buildFromCatalog(
                    MessageCodes.Domain.Pagination.INVALID_PAGE_TECHNICAL,
                    MessageCodes.Domain.Pagination.INVALID_PAGE_USER);
        }
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw DomainException.buildFromCatalog(
                    MessageCodes.Domain.Pagination.INVALID_SIZE_TECHNICAL,
                    MessageCodes.Domain.Pagination.INVALID_SIZE_USER);
        }
    }

    public static PageCriteria of(final int page, final int size) {
        return new PageCriteria(page, size);
    }

    public int offset() {
        return page * size;
    }
}
