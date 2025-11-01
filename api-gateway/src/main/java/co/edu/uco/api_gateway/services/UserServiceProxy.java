package co.edu.uco.api_gateway.services;

import co.edu.uco.api_gateway.dto.PageResponse;
import co.edu.uco.api_gateway.dto.UserCreateRequest;
import co.edu.uco.api_gateway.dto.UserDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class UserServiceProxy {

    private static final ParameterizedTypeReference<PageResponse<UserDto>> PAGE_OF_USER =
            new ParameterizedTypeReference<>() {};

    private final WebClient webClient;

    public UserServiceProxy(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("lb://UCOCHALLENGE/uco-challenge/api/v1")
                .build();
    }

    public PageResponse<UserDto> getAllUsers(Map<String, String> queryParams, String authorizationHeader) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/users");
                    queryParams.forEach((key, value) -> {
                        if (StringUtils.hasText(value)) {
                            uriBuilder.queryParam(key, value);
                        }
                    });
                    return uriBuilder.build();
                })
                .headers(httpHeaders -> setAuthorization(httpHeaders, authorizationHeader))
                .retrieve()
                .bodyToMono(PAGE_OF_USER)
                .block();
    }

    public UserDto createUser(UserCreateRequest user, String authorizationHeader) {
        return webClient.post()
                .uri("/users")
                .headers(httpHeaders -> setAuthorization(httpHeaders, authorizationHeader))
                .bodyValue(user)
                .retrieve()
                .bodyToMono(UserDto.class)
                .block();
    }

    private void setAuthorization(HttpHeaders headers, String authorizationHeader) {
        if (StringUtils.hasText(authorizationHeader)) {
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
    }
}
