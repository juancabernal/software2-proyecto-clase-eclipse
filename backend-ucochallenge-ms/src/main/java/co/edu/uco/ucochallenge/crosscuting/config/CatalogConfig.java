package co.edu.uco.ucochallenge.crosscuting.config;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

@Configuration
public class CatalogConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(5);

    @Bean
    public WebClient messageCatalogWebClient(final WebClient.Builder builder,
            @Value("${services.message.base-url}") final String baseUrl) {
        return createClient(builder, baseUrl);
    }

    @Bean
    public WebClient parameterCatalogWebClient(final WebClient.Builder builder,
            @Value("${services.parameters.base-url}") final String baseUrl) {
        return createClient(builder, baseUrl);
    }

    private WebClient createClient(final WebClient.Builder builder, final String baseUrl) {
        final HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) CONNECT_TIMEOUT.toMillis())
                .responseTimeout(RESPONSE_TIMEOUT)
                .doOnConnected(connection -> connection
                        .addHandlerLast(new ReadTimeoutHandler(RESPONSE_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(RESPONSE_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)));

        return builder
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .defaultHeader(HttpHeaders.PRAGMA, "no-cache")
                .defaultHeader(HttpHeaders.EXPIRES, "0")
                .exchangeStrategies(ExchangeStrategies.builder().codecs(configurer ->
                        configurer.defaultCodecs().maxInMemorySize(512 * 1024))
                        .build())
                .build();
    }
}
