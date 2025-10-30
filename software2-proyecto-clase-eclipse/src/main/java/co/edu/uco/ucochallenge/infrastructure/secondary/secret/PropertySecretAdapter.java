package co.edu.uco.ucochallenge.infrastructure.secondary.secret;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import co.edu.uco.ucochallenge.domain.secret.port.SecretProviderPort;

/**
 * Simple {@link SecretProviderPort} implementation that resolves secrets
 * directly from the Spring environment. The bean is always available so the
 * application can bootstrap even when Azure Key Vault is disabled or
 * temporarily unreachable. When the {@link AzureKeyVaultSecretAdapter} is
 * active it is marked as {@code @Primary}, so this adapter transparently acts
 * as a fallback.
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
            log.warn("Secret '{}' is not configured. Set property '{}{}' or provide it through Azure Key Vault.", name,
                    SECRET_PREFIX, name);
        }
        return value;
    }
}
