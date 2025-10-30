package co.edu.uco.ucochallenge.infrastructure.secondary.adapters.service.client;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.service.dto.RemoteCatalogEntry;
import reactor.core.publisher.Mono;

import org.springframework.web.reactive.function.client.WebClient;

@Component
public class ParameterCatalogClient {

    private static final Logger log = LoggerFactory.getLogger(ParameterCatalogClient.class);

    private final WebClient webClient;

    public ParameterCatalogClient(@Qualifier("parameterCatalogWebClient") final WebClient webClient) {
        this.webClient = webClient;
    }

    public Optional<String> findValueByKey(final String key) {
        log.debug("Requesting parameter '{}' from remote catalog", key);

        final RemoteCatalogEntry entry = webClient.get()
                .uri(builder -> builder.pathSegment(key).build())
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.empty();
                    }
                    if (response.statusCode().isError()) {
                        return response.createException().flatMap(Mono::error);
                    }
                    return response.bodyToMono(RemoteCatalogEntry.class);
                })
                .block();

        if (entry == null || TextHelper.isEmpty(entry.value())) {
            log.debug("Remote catalog did not return a value for parameter '{}'", key);
            return Optional.empty();
        }

        log.debug("Remote catalog returned value for parameter '{}': '{}'", key, entry.value());
        return Optional.of(entry.value());
    }
}
