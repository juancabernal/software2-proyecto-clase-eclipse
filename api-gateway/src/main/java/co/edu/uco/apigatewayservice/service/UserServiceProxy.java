package co.edu.uco.apigatewayservice.service;

import java.net.URI;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

@Service
public class UserServiceProxy {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final WebClient userServiceClient;

    public UserServiceProxy(WebClient userServiceWebClient) {
        this.userServiceClient = userServiceWebClient;
    }

    public Mono<ResponseEntity<JsonNode>> createUser(JsonNode body, HttpHeaders incomingHeaders) {
        return exchangeJson(HttpMethod.POST, builder -> builder.path("/users").build(), body, incomingHeaders);
    }

    public Mono<ResponseEntity<JsonNode>> fetchUsers(Integer page, Integer size, HttpHeaders incomingHeaders) {
        return exchangeJson(HttpMethod.GET, builder -> {
            UriBuilder uriBuilder = builder.path("/users");
            if (page != null) {
                uriBuilder = uriBuilder.queryParam("page", page);
            }
            if (size != null) {
                uriBuilder = uriBuilder.queryParam("size", size);
            }
            return uriBuilder.build();
        }, null, incomingHeaders);
    }

    public Mono<ResponseEntity<Void>> sendVerificationCode(String userId, String channel, HttpHeaders incomingHeaders) {
        return exchangeVoid(HttpMethod.POST, builder -> builder
            .path("/users/{id}/send-code")
            .queryParam("channel", channel)
            .build(userId), incomingHeaders);
    }

    public Mono<ResponseEntity<JsonNode>> confirmVerificationCode(String userId, JsonNode body, HttpHeaders incomingHeaders) {
        return exchangeJson(HttpMethod.POST, builder -> builder
            .path("/users/{id}/confirm-code")
            .build(userId), body, incomingHeaders);
    }

    private Mono<ResponseEntity<JsonNode>> exchangeJson(HttpMethod method,
                                                        Function<UriBuilder, URI> uriFunction,
                                                        JsonNode body,
                                                        HttpHeaders incomingHeaders) {
        WebClient.RequestBodySpec requestSpec = userServiceClient.method(method).uri(uriFunction);
        requestSpec.headers(outgoing -> copyForwardedHeaders(outgoing, incomingHeaders, method, body != null));

        WebClient.RequestHeadersSpec<?> finalSpec = requiresBody(method) && body != null
            ? requestSpec.bodyValue(body)
            : requestSpec;

        return finalSpec.exchangeToMono(this::toJsonResponse);
    }

    private Mono<ResponseEntity<Void>> exchangeVoid(HttpMethod method,
                                                    Function<UriBuilder, URI> uriFunction,
                                                    HttpHeaders incomingHeaders) {
        return userServiceClient
            .method(method)
            .uri(uriFunction)
            .headers(outgoing -> copyForwardedHeaders(outgoing, incomingHeaders, method, false))
            .exchangeToMono(response -> response
                .releaseBody()
                .then(Mono.fromSupplier(() -> buildEmptyResponse(response))));
    }

    private ResponseEntity<Void> buildEmptyResponse(ClientResponse response) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(response.statusCode());
        HttpHeaders headers = response.headers().asHttpHeaders();
        headers.forEach((name, values) -> builder.header(name, values.toArray(String[]::new)));
        return builder.build();
    }

    private Mono<ResponseEntity<JsonNode>> toJsonResponse(ClientResponse response) {
        return response.bodyToMono(JsonNode.class)
            .map(Optional::of)
            .defaultIfEmpty(Optional.empty())
            .map(optionalBody -> {
                ResponseEntity.BodyBuilder builder = ResponseEntity.status(response.statusCode());
                HttpHeaders headers = response.headers().asHttpHeaders();
                headers.forEach((name, values) -> builder.header(name, values.toArray(String[]::new)));
                return optionalBody
                    .<ResponseEntity<JsonNode>>map(builder::body)
                    .orElseGet(() -> builder.<JsonNode>build());
            });
    }

    private void copyForwardedHeaders(HttpHeaders target,
                                      HttpHeaders source,
                                      HttpMethod method,
                                      boolean hasBody) {
        copyHeader(HttpHeaders.AUTHORIZATION, target, source);
        copyHeader(IDEMPOTENCY_KEY_HEADER, target, source);
        if (hasBody && !target.containsKey(HttpHeaders.CONTENT_TYPE)) {
            target.set(HttpHeaders.CONTENT_TYPE, "application/json");
        }
        target.set(HttpHeaders.ACCEPT, "application/json");
        if (HttpMethod.GET.equals(method)) {
            target.remove(HttpHeaders.CONTENT_TYPE);
        }
    }

    private void copyHeader(String headerName, HttpHeaders target, HttpHeaders source) {
        List<String> values = source.get(headerName);
        if (!CollectionUtils.isEmpty(values)) {
            target.put(headerName, List.copyOf(values));
        }
    }

    private boolean requiresBody(HttpMethod method) {
        Set<HttpMethod> methods = EnumSet.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH);
        return methods.contains(method);
    }
}
