package co.edu.uco.api_gateway.services;

import java.util.List;
import java.util.Objects;

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
import co.edu.uco.api_gateway.dto.CatalogItemDto;
import co.edu.uco.api_gateway.exception.DownstreamException;
import reactor.core.publisher.Mono;

@Service
public class CatalogServiceProxy {

    private static final String CATALOG_BASE_PATH = "lb://UCOCHALLENGE/uco-challenge/api/v1/catalogs";
    private static final ParameterizedTypeReference<ApiSuccessResponse<List<CatalogItemDto>>> CATALOG_RESPONSE =
            new ParameterizedTypeReference<>() {};

    private final WebClient webClient;

    public CatalogServiceProxy(final WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(CATALOG_BASE_PATH)
                .build();
    }

    /**
     * Recupera el catálogo de tipos de identificación disponible en el backend.
     *
     * <p>Ejemplo:
     * <pre>{@code
     * ApiSuccessResponse<List<CatalogItemDto>> response =
     *         catalogServiceProxy.listIdTypes("Bearer eyJhbGciOi...");
     * }</pre>
     */
    public ApiSuccessResponse<List<CatalogItemDto>> listIdTypes(final String authorizationHeader) {
        return getCatalog("/id-types", authorizationHeader);
    }

    /**
     * Recupera el catálogo de ciudades disponible en el backend.
     */
    public ApiSuccessResponse<List<CatalogItemDto>> listCities(final String authorizationHeader) {
        return getCatalog("/cities", authorizationHeader);
    }

    /**
     * Recupera el catálogo de departamentos disponible en el backend.
     */
    public ApiSuccessResponse<List<CatalogItemDto>> listDepartments(final String authorizationHeader) {
        return getCatalog("/departments", authorizationHeader);
    }

    public ApiSuccessResponse<List<CatalogItemDto>> listCitiesByDepartment(
            final String authorizationHeader,
            final String departmentId) {
        return getCatalog("/departments/" + departmentId + "/cities", authorizationHeader);
    }

    private ApiSuccessResponse<List<CatalogItemDto>> getCatalog(
            final String path,
            final String authorizationHeader) {
        final ApiSuccessResponse<List<CatalogItemDto>> response = webClient.get()
                .uri(path)
                .headers(headers -> {
                    setAuthorization(headers, authorizationHeader);
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(CATALOG_RESPONSE)
                .block();

        return Objects.requireNonNull(response, "La respuesta del catálogo no puede ser nula");
    }

    private void setAuthorization(final HttpHeaders headers, final String authorizationHeader) {
        if (StringUtils.hasText(authorizationHeader)) {
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
    }

    private Mono<? extends Throwable> mapError(final ClientResponse response) {
        return response.bodyToMono(ApiErrorResponse.class)
                .defaultIfEmpty(ApiErrorResponse.of(
                        response.statusCode().value(),
                        response.statusCode().toString(),
                        response.statusCode().toString()))
                .flatMap(error -> Mono.error(new DownstreamException(response.statusCode(), error)));
    }
}
