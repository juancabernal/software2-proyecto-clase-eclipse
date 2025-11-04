package co.edu.uco.api_gateway.services;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import co.edu.uco.api_gateway.dto.ApiErrorResponse;
import co.edu.uco.api_gateway.dto.ApiSuccessResponse;
import co.edu.uco.api_gateway.dto.GetUserResponse;
import co.edu.uco.api_gateway.dto.ListUsersResponse;
import co.edu.uco.api_gateway.dto.PageResponse;
import co.edu.uco.api_gateway.dto.PaginationMetadataDto;
import co.edu.uco.api_gateway.dto.RegisterUserResponse;
import co.edu.uco.api_gateway.dto.UserCreateRequest;
import co.edu.uco.api_gateway.dto.UserDto;
import co.edu.uco.api_gateway.exception.DownstreamException;
import reactor.core.publisher.Mono;

@Service
public class UserServiceProxy {

    private static final String USERS_BASE_PATH = "lb://UCOCHALLENGE/uco-challenge/api/v1/users";
    private static final Set<String> PAGINATION_PARAMS = Set.of("page", "size");

    private static final ParameterizedTypeReference<ApiSuccessResponse<ListUsersResponse>> LIST_USERS_RESPONSE =
            new ParameterizedTypeReference<>() {
            };

    private static final ParameterizedTypeReference<ApiSuccessResponse<RegisterUserResponse>> REGISTER_USER_RESPONSE =
            new ParameterizedTypeReference<>() {
            };

    private static final ParameterizedTypeReference<ApiSuccessResponse<GetUserResponse>> GET_USER_RESPONSE =
            new ParameterizedTypeReference<>() {
            };

    private static final ParameterizedTypeReference<ApiSuccessResponse<Void>> VOID_RESPONSE =
            new ParameterizedTypeReference<>() {
            };

    private final WebClient webClient;


    public UserServiceProxy(final WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(USERS_BASE_PATH)
                .build();
    }

    /**
     * Realiza la llamada al microservicio {@code uco-challenge} para registrar un usuario.
     *
     * <p>Ejemplo de uso:
     * <pre>{@code
     * ApiSuccessResponse<RegisterUserResponse> response = userServiceProxy.createUser(
     *         userCreateRequest,
     *         "Bearer eyJhbGciOi...");
     * }</pre>
     */
    public ApiSuccessResponse<RegisterUserResponse> createUser(
            final UserCreateRequest user,
            final String authorizationHeader) {
        final ApiSuccessResponse<RegisterUserResponse> response = webClient.post()
                .headers(httpHeaders -> {
                    setAuthorization(httpHeaders, authorizationHeader);
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                })
                .bodyValue(user)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(REGISTER_USER_RESPONSE)
                .block();

        return Objects.requireNonNull(response, "La respuesta de creación de usuario no puede ser nula");
    }

    /**
     * Obtiene una página de usuarios del backend.<br>
     * Ejemplo:
     * <pre>{@code
     * ApiSuccessResponse<PageResponse<UserDto>> response = userServiceProxy.getAllUsers(
     *         Map.of("page", "0", "size", "20"),
     *         "Bearer eyJhbGciOi...");
     * }</pre>
     */
    public ApiSuccessResponse<PageResponse<UserDto>> getAllUsers(
            final Map<String, String> queryParams,
            final String authorizationHeader) {
        final ApiSuccessResponse<ListUsersResponse> response = webClient.get()
                .uri(uriBuilder -> {
                    PAGINATION_PARAMS.forEach(param -> {
                        final String value = queryParams.get(param);
                        if (StringUtils.hasText(value)) {
                            uriBuilder.queryParam(param, value);
                        }
                    });
                    return uriBuilder.build();
                })
                .headers(httpHeaders -> setAuthorization(httpHeaders, authorizationHeader))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(LIST_USERS_RESPONSE)
                .block();

        return buildPageResponse(response);
    }

    /**
     * Recupera un usuario por su identificador.<br>
     * Ejemplo:
     * <pre>{@code
     * ApiSuccessResponse<GetUserResponse> response = userServiceProxy.getUserById(
     *         UUID.fromString("e2a0f9e2-d543-4f0e-b82c-98cbfb9126b0"),
     *         "Bearer eyJhbGciOi...");
     * }</pre>
     */
    public ApiSuccessResponse<GetUserResponse> getUserById(
            final UUID id,
            final String authorizationHeader) {
        final ApiSuccessResponse<GetUserResponse> response = webClient.get()
                .uri("/{id}", id)
                .headers(httpHeaders -> setAuthorization(httpHeaders, authorizationHeader))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(GET_USER_RESPONSE)
                .block();

        return Objects.requireNonNull(response, "La respuesta de consulta de usuario no puede ser nula");
    }


    /**
     * Busca usuarios aplicando filtros y paginación.
     *
     * <p>Ejemplo de invocación:
     * <pre>{@code
     * ApiSuccessResponse<PageResponse<UserDto>> response = userServiceProxy.searchUsers(
     *         Map.of("email", "juan@example.com", "page", "0"),
     *         "Bearer eyJhbGciOi...");
     * }</pre>
     */
    public ApiSuccessResponse<PageResponse<UserDto>> searchUsers(
            final Map<String, String> filters,
            final String authorizationHeader) {
        final ApiSuccessResponse<ListUsersResponse> response = webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/search");
                    filters.forEach((key, value) -> {
                        if (StringUtils.hasText(value)) {
                            uriBuilder.queryParam(key, value);
                        }
                    });
                    return uriBuilder.build();
                })
                .headers(httpHeaders -> setAuthorization(httpHeaders, authorizationHeader))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(LIST_USERS_RESPONSE)
                .block();

        return buildPageResponse(response);
    }

    /**
     * Elimina un usuario en el backend.<br>
     * Ejemplo:
     * <pre>{@code
     * ApiSuccessResponse<Void> response = userServiceProxy.deleteUser(
     *         UUID.fromString("e2a0f9e2-d543-4f0e-b82c-98cbfb9126b0"),
     *         "Bearer eyJhbGciOi...");
     * }</pre>
     */
    public ApiSuccessResponse<Void> deleteUser(
            final UUID id,
            final String authorizationHeader) {
        final ApiSuccessResponse<Void> response = webClient.delete()
                .uri("/{id}", id)
                .headers(httpHeaders -> setAuthorization(httpHeaders, authorizationHeader))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(VOID_RESPONSE)
                .block();

        return Objects.requireNonNull(response, "La respuesta de eliminación de usuario no puede ser nula");
    }

    public ApiSuccessResponse<Void> requestEmailConfirmation(
            final UUID id,
            final String authorizationHeader) {
        final ApiSuccessResponse<Void> response = webClient.post()
                .uri("/{id}/confirmations/email", id)
                .headers(httpHeaders -> setAuthorization(httpHeaders, authorizationHeader))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(VOID_RESPONSE)
                .block();

        return Objects.requireNonNull(response, "La respuesta de solicitud de confirmación de correo no puede ser nula");
    }

    public ApiSuccessResponse<Void> requestMobileConfirmation(
            final UUID id,
            final String authorizationHeader) {
        final ApiSuccessResponse<Void> response = webClient.post()
                .uri("/{id}/confirmations/mobile", id)
                .headers(httpHeaders -> setAuthorization(httpHeaders, authorizationHeader))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(VOID_RESPONSE)
                .block();

        return Objects.requireNonNull(response, "La respuesta de solicitud de confirmación de teléfono no puede ser nula");
    }


    private ApiSuccessResponse<PageResponse<UserDto>> buildPageResponse(
            final ApiSuccessResponse<ListUsersResponse> response) {
        final ApiSuccessResponse<ListUsersResponse> nonNullResponse = Objects.requireNonNull(
                response,
                "La respuesta del servicio de usuarios no puede ser nula");

        final ListUsersResponse data = Objects.requireNonNull(
                nonNullResponse.data(),
                "El cuerpo de la respuesta no puede ser nulo");
        final PaginationMetadataDto pagination = Objects.requireNonNull(
                data.pagination(),
                "La metadata de paginación no puede ser nula");

        final PageResponse<UserDto> pageResponse = new PageResponse<>(
                data.users(),
                pagination.page(),
                pagination.size(),
                pagination.totalElements(),
                pagination.totalPages(),
                pagination.hasNext(),
                pagination.hasPrevious());

        return new ApiSuccessResponse<>(nonNullResponse.userMessage(), pageResponse);
    }

    private void setAuthorization(final HttpHeaders headers, final String authorizationHeader) {
        if (StringUtils.hasText(authorizationHeader)) {
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
    }

    private Mono<? extends Throwable> mapError(final ClientResponse response) {
        final var contentType = response.headers().contentType().orElse(null);
        return response.bodyToMono(byte[].class)
                .defaultIfEmpty(new byte[0])
                .flatMap(bytes -> {
                    String body = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                    ApiErrorResponse parsed = null;
                    // Si es JSON, intentamos parsear; si falla, seguimos con texto plano
                    if (contentType != null && MediaType.APPLICATION_JSON.includes(contentType)) {
                        try {
                            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                            parsed = om.readValue(bytes, ApiErrorResponse.class);
                        } catch (Exception ignored) { /* cae a texto plano */ }
                    }
                    if (parsed == null) {
                        String msg = "Upstream error %d %s".formatted(
                                response.statusCode().value(), response.statusCode());
                        if (!body.isBlank()) {
                            msg += " - body: " + body;
                        }
                        parsed = ApiErrorResponse.of(
                                response.statusCode().value(),
                                "UPSTREAM_ERROR",
                                msg
                        );
                    }
                    return Mono.error(new DownstreamException(response.statusCode(), parsed));
                });
    }
}
