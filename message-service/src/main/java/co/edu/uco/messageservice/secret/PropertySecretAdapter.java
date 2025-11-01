package co.edu.uco.messageservice.secret;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Default {@link SecretProviderPort} that resolves secret values directly from
 * the Spring {@link Environment}. It is always available so the application can
 * start even when Azure Key Vault is not reachable.
 */
@Component
public class PropertySecretAdapter implements SecretProviderPort {

    private static final Logger log = LoggerFactory.getLogger(PropertySecretAdapter.class);
    private static final String SECRET_PREFIX = "secrets.";

    private final Environment environment;

    public PropertySecretAdapter(final Environment environment) {
        this.environment = environment;
        log.info("Using property-based secret provider. Set 'azure.keyvault.url' to enable Azure Key Vault integration.");
    }

    @Override
    public String getSecret(final String name) {
        final String value = environment.getProperty(SECRET_PREFIX + name, "");
        if (!StringUtils.hasText(value)) {
            log.warn("Secret '{}' is not configured. Define property '{}{}' or provide it through Azure Key Vault.", name,
                    SECRET_PREFIX, name);
        }
        return value;
    }
}
