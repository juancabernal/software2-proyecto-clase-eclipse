package co.edu.uco.ucochallenge.infrastructure.secondary.adapters.service.orchestration;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.service.client.MessageCatalogClient;
import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.service.client.ParameterCatalogClient;

@Service
public class CatalogService {

    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);

    private final MessageCatalogClient messageCatalogClient;
    private final ParameterCatalogClient parameterCatalogClient;

    public CatalogService(final MessageCatalogClient messageCatalogClient,
            final ParameterCatalogClient parameterCatalogClient) {
        this.messageCatalogClient = messageCatalogClient;
        this.parameterCatalogClient = parameterCatalogClient;
    }

    public Optional<String> findMessageValue(final String key, final Map<String, String> parameters) {
        log.debug("Delegating lookup of message '{}' with parameters {}", key, parameters.keySet());
        final Optional<String> value = messageCatalogClient.findValueByKey(key, parameters);
        value.ifPresentOrElse(found -> log.debug("Catalog returned value for message '{}': '{}'", key, found),
                () -> log.debug("Catalog did not return a value for message '{}'", key));
        return value;
    }

    public Optional<String> findParameterValue(final String key) {
        log.debug("Delegating lookup of parameter '{}'", key);
        final Optional<String> value = parameterCatalogClient.findValueByKey(key);
        value.ifPresentOrElse(found -> log.debug("Catalog returned value for parameter '{}': '{}'", key, found),
                () -> log.debug("Catalog did not return a value for parameter '{}'", key));
        return value;
    }
}
