package co.edu.uco.api_gateway.dto;

import java.util.List;

public record ListUsersResponse(List<UserDto> users, PaginationMetadataDto pagination) {
}
