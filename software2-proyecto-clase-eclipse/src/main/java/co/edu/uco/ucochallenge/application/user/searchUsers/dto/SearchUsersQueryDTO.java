package co.edu.uco.ucochallenge.application.user.searchUsers.dto;

import java.util.UUID;

import co.edu.uco.ucochallenge.application.pagination.dto.PaginationRequestDTO;
import co.edu.uco.ucochallenge.domain.user.model.UserSearchQuery;

public record SearchUsersQueryDTO(SearchUsersFilterDTO filter, PaginationRequestDTO pagination) {

    public static SearchUsersQueryDTO normalize(
            final UUID idType,
            final String idNumber,
            final String firstName,
            final String firstSurname,
            final UUID homeCity,
            final String email,
            final String mobileNumber,
            final String q,
            final Integer page,
            final Integer size) {
        final SearchUsersFilterDTO normalizedFilter = SearchUsersFilterDTO.normalize(
                idType, idNumber, firstName, firstSurname, homeCity, email, mobileNumber, q);
        final PaginationRequestDTO normalizedPagination = PaginationRequestDTO.normalize(page, size);
        return new SearchUsersQueryDTO(normalizedFilter, normalizedPagination);
    }

    public static SearchUsersQueryDTO normalize(final SearchUsersQueryDTO dto) {
        if (dto == null) {
            return normalize(null, null, null, null, null, null, null, null, null, null);
        }
        final SearchUsersFilterDTO normalizedFilter = SearchUsersFilterDTO.normalize(dto.filter());
        final PaginationRequestDTO normalizedPagination = dto.pagination() == null
                ? PaginationRequestDTO.normalize(null, null)
                : PaginationRequestDTO.normalize(dto.pagination().page(), dto.pagination().size());
        return new SearchUsersQueryDTO(normalizedFilter, normalizedPagination);
    }

    public UserSearchQuery toDomain() {
        return new UserSearchQuery(filter.toDomain(), pagination.toDomain());
    }

    // ✅ NUEVO: clonar con nueva paginación
    public SearchUsersQueryDTO withPagination(final PaginationRequestDTO newPagination) {
        final PaginationRequestDTO norm = PaginationRequestDTO.normalize(
                newPagination != null ? newPagination.page() : null,
                newPagination != null ? newPagination.size() : null
        );
        return new SearchUsersQueryDTO(this.filter, norm);
    }

    // ✅ Overload práctico
    public SearchUsersQueryDTO withPagination(final int page, final int size) {
        return new SearchUsersQueryDTO(this.filter, PaginationRequestDTO.normalize(page, size));
    }
}
