package co.edu.uco.api_gateway.services;

import co.edu.uco.api_gateway.dto.ApiSuccessResponse;
import co.edu.uco.api_gateway.dto.ListUsersResponse;
import co.edu.uco.api_gateway.dto.PageResponse;
import co.edu.uco.api_gateway.dto.PaginationMetadataDto;
import co.edu.uco.api_gateway.dto.RegisterUserResponse;
import co.edu.uco.api_gateway.dto.UserCreateRequest;
import co.edu.uco.api_gateway.dto.UserDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class UserServiceProxy {

    private static final ParameterizedTypeReference<ApiSuccessResponse<ListUsersResponse>> LIST_USERS_RESPONSE =
            new ParameterizedTypeReference<>() {};

    private static final ParameterizedTypeReference<ApiSuccessResponse<RegisterUserResponse>> REGISTER_USER_RESPONSE =
            new ParameterizedTypeReference<>() {};

    private static final ParameterizedTypeReference<List<Map<String, Object>>> RAW_COLLECTION_RESPONSE =
            new ParameterizedTypeReference<>() {};

    private final WebClient webClient;

    public UserServiceProxy(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("lb://UCOCHALLENGE/uco-challenge/api/v1")
                .build();
    }

    public ApiSuccessResponse<PageResponse<UserDto>> getAllUsers(Map<String, String> queryParams, String authorizationHeader) {
        final String page = queryParams.get("page");
        final String size = queryParams.get("size");

        final ApiSuccessResponse<ListUsersResponse> response = webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/users");
                    if (StringUtils.hasText(page)) {
                        uriBuilder.queryParam("page", page);
                    }
                    if (StringUtils.hasText(size)) {
                        uriBuilder.queryParam("size", size);
                    }
                    return uriBuilder.build();
                })
                .headers(httpHeaders -> setAuthorization(httpHeaders, authorizationHeader))
                .retrieve()
                .bodyToMono(LIST_USERS_RESPONSE)
                .block();

        Objects.requireNonNull(response, "La respuesta del servicio de usuarios no puede ser nula");
        final ListUsersResponse data = response.data();
        final PaginationMetadataDto pagination = data.pagination();
        final PageResponse<UserDto> pageResponse = new PageResponse<>(
                data.users(),
                pagination.page(),
                pagination.size(),
                pagination.totalElements(),
                pagination.totalPages());

        return new ApiSuccessResponse<>(response.userMessage(), pageResponse);
    }

    public ApiSuccessResponse<RegisterUserResponse> createUser(UserCreateRequest user, String authorizationHeader) {
        final ApiSuccessResponse<RegisterUserResponse> response = webClient.post()
                .uri("/users")
                .headers(httpHeaders -> {
                    setAuthorization(httpHeaders, authorizationHeader);
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                })
                .bodyValue(user)
                .retrieve()
                .bodyToMono(REGISTER_USER_RESPONSE)
                .block();

        Objects.requireNonNull(response, "La respuesta de creación de usuario no puede ser nula");
        return response;
    }

    public List<Map<String, Object>> listDepartments(String authorizationHeader) {
        return fetchCollection("/users/departments", authorizationHeader);
    }

    public List<Map<String, Object>> listCities(String authorizationHeader) {
        return fetchCollection("/users/cities", authorizationHeader);
    }

    public List<Map<String, Object>> listIdTypes(String authorizationHeader) {
        return fetchCollection("/users/id-types", authorizationHeader);
    }

    private List<Map<String, Object>> fetchCollection(String path, String authorizationHeader) {
        final List<Map<String, Object>> response = webClient.get()
                .uri(path)
                .headers(httpHeaders -> setAuthorization(httpHeaders, authorizationHeader))
                .retrieve()
                .bodyToMono(RAW_COLLECTION_RESPONSE)
                .block();

        return Objects.requireNonNull(response, "La respuesta del catálogo no puede ser nula");
    }

    private void setAuthorization(HttpHeaders headers, String authorizationHeader) {
        if (StringUtils.hasText(authorizationHeader)) {
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
    }
}
