package co.edu.uco.ucochallenge.application.user.listUsers.dto;

import co.edu.uco.ucochallenge.application.pagination.dto.PaginationRequestDTO;

public record ListUsersRequestDTO(PaginationRequestDTO pagination) {

    public static ListUsersRequestDTO of(final PaginationRequestDTO pagination) {
        return new ListUsersRequestDTO(pagination);
    }
}
