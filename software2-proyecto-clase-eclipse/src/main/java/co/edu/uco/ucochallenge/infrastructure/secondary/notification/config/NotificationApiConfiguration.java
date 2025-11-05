package co.edu.uco.ucochallenge.infrastructure.secondary.notification.config;

import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(NotificationApiProperties.class)
public class NotificationApiConfiguration {

    @Bean
    RestTemplate notificationRestTemplate(final RestTemplateBuilder builder,
            final NotificationApiProperties properties) {
        final Duration connectTimeout = properties.getConnectTimeout();
        final Duration readTimeout = properties.getReadTimeout();
        return builder
                .setConnectTimeout(connectTimeout)
                .setReadTimeout(readTimeout)
                .build();
    }
}
