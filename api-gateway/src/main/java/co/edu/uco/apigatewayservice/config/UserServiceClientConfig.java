package co.edu.uco.apigatewayservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class UserServiceClientConfig {

    @Bean
    public WebClient userServiceWebClient(WebClient.Builder builder,
                                          @Value("${gateway.services.users.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}
