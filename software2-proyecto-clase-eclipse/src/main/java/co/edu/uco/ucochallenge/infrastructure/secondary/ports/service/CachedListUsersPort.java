package co.edu.uco.ucochallenge.infrastructure.secondary.ports.service;

import co.edu.uco.ucochallenge.application.pagination.dto.PaginationRequestDTO;
import co.edu.uco.ucochallenge.application.user.listUsers.dto.ListUsersResponseDTO;

public interface CachedListUsersPort {
    ListUsersResponseDTO getPage(PaginationRequestDTO pagination);
}
