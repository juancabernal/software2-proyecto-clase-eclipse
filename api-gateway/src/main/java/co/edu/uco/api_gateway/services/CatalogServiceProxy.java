package co.edu.uco.api_gateway.services;

import java.util.List;
import java.util.Objects;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import co.edu.uco.api_gateway.dto.ApiSuccessResponse;
import co.edu.uco.api_gateway.dto.CatalogItemDto;

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
                .bodyToMono(CATALOG_RESPONSE)
                .block();

        return Objects.requireNonNull(response, "La respuesta del catálogo no puede ser nula");
    }

    private void setAuthorization(final HttpHeaders headers, final String authorizationHeader) {
        if (StringUtils.hasText(authorizationHeader)) {
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
    }
}
