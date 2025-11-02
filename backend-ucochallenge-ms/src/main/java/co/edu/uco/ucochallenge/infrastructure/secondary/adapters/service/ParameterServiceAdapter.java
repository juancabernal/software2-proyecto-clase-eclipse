package co.edu.uco.ucochallenge.infrastructure.secondary.adapters.service;

import java.util.Collections;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import co.edu.uco.ucochallenge.crosscuting.exception.InfrastructureException;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageCodes;
import co.edu.uco.ucochallenge.crosscuting.parameter.ParameterServicePortHolder;
import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.service.orchestration.CatalogService;
import co.edu.uco.ucochallenge.infrastructure.secondary.ports.service.ParameterServicePort;

@Component
public class ParameterServiceAdapter implements ParameterServicePort {

    private static final Logger log = LoggerFactory.getLogger(ParameterServiceAdapter.class);

    private final CatalogService catalogService;

    public ParameterServiceAdapter(final CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @PostConstruct
    void configureHolder() {
        ParameterServicePortHolder.configure(this);
    }

    @Override
    public String getParameter(final String key) {
        if (TextHelper.isEmpty(key)) {
            return TextHelper.getDefault();
        }

        try {
            log.debug("Fetching parameter '{}' from remote catalog", key);
            return catalogService.findParameterValue(key)
                    .filter(value -> !TextHelper.isEmpty(value))
                    .orElse(TextHelper.getDefault());
        } catch (final WebClientResponseException.NotFound notFound) {
            log.info("Parameter '{}' not found in remote catalog", key);
            return TextHelper.getDefault();
        } catch (final WebClientRequestException | WebClientResponseException exception) {
            log.error("Failed to fetch parameter '{}' from remote catalog", key, exception);
            throw InfrastructureException.buildFromCatalog(
                    MessageCodes.Infrastructure.ParameterService.UNAVAILABLE_TECHNICAL,
                    MessageCodes.Infrastructure.ParameterService.UNAVAILABLE_USER,
                    Collections.emptyMap(), exception);
        }
    }
}
