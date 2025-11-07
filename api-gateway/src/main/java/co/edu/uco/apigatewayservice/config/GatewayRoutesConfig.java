package co.edu.uco.apigatewayservice.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // ✅ Redirección general del frontend al backend
            .route("ucochallenge-backend", r -> r
                .path("/api/**")
                // ⚡ FIX: maneja bien rutas con o sin subsegmentos (/api, /api/, /api/users)
                .filters(f -> f
                    .rewritePath("/api(?<segment>/?.*)", "/uco-challenge/api/v1${segment}")
                )
                .uri("http://localhost:8081")
            )
            // ✅ Endpoints internos del gateway
            .route("gateway-admin", r -> r
                .path("/api/admin/**")
                .uri("no://op")
            )
            .build();
    }
}
