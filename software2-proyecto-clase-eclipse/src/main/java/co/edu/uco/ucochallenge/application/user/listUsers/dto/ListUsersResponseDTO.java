package co.edu.uco.ucochallenge.application.user.listUsers.dto;

import java.util.List;

import co.edu.uco.ucochallenge.application.pagination.dto.PaginationMetadataDTO;

public record ListUsersResponseDTO(List<ListUsersOutputDTO> users, PaginationMetadataDTO pagination) {
}
