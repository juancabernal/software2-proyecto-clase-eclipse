package co.edu.uco.apigatewayservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import co.edu.uco.apigatewayservice.dto.RegisterUserRequest;
import co.edu.uco.apigatewayservice.dto.RegisterUserResponse;
import reactor.core.publisher.Mono;

@Service
public class UserRegistrationGatewayService {

    private final WebClient webClient;

    public UserRegistrationGatewayService(WebClient.Builder builder,
            @Value("${services.ucochallenge.base-url}") String backendBaseUrl) {
        this.webClient = builder.baseUrl(backendBaseUrl).build();
    }

    public Mono<ResponseEntity<?>> register(final RegisterUserRequest request) {
        return webClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(RegisterUserResponse.class)
                                .map(body -> ResponseEntity.status(response.statusCode())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(body));
                    }

                    return response.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .map(body -> {
                                ResponseEntity.BodyBuilder builder = ResponseEntity.status(response.statusCode());
                                builder.contentType(response.headers().contentType()
                                        .orElse(MediaType.APPLICATION_JSON));
                                if (body.isEmpty()) {
                                    return builder.build();
                                }
                                return builder.body(body);
                            });
                });
    }
}
