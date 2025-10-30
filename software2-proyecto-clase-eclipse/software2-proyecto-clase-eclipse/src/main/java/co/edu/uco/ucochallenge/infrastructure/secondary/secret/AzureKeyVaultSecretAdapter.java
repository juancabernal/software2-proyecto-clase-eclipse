package co.edu.uco.ucochallenge.infrastructure.secondary.secret;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;

import co.edu.uco.ucochallenge.domain.secret.port.SecretProviderPort;

/**
 * Secondary adapter that retrieves secrets from Azure Key Vault.
 */
@Component
@Primary
@ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${azure.keyvault.url:}')")
public class AzureKeyVaultSecretAdapter implements SecretProviderPort {

    private static final Logger log = LoggerFactory.getLogger(AzureKeyVaultSecretAdapter.class);
    private static final String SECRET_PREFIX = "secrets.";

    private final SecretClient client;
    private final Environment environment;

    public AzureKeyVaultSecretAdapter(@Value("${azure.keyvault.url}") final String vaultUrl,
            final Environment environment) {
        this.environment = environment;
        this.client = new SecretClientBuilder()
                .vaultUrl(vaultUrl)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
        log.info("Azure Key Vault initialized at {}", vaultUrl);
    }

    @Override
    public String getSecret(final String name) {
        try {
            final KeyVaultSecret secret = client.getSecret(name);
            final String value = secret != null ? secret.getValue() : null;
            if (StringUtils.hasText(value)) {
                log.debug("Secret '{}' successfully retrieved from Azure Key Vault", name);
                return value;
            }
            log.warn("Secret '{}' retrieved from Azure Key Vault is empty. Falling back to application properties.", name);
        } catch (Exception exception) {
            log.warn("Unable to retrieve secret '{}' from Azure Key Vault. Using local configuration. Cause: {}", name,
                    exception.getMessage());
            log.debug("Azure Key Vault exception", exception);
        }

        final String fallback = environment.getProperty(SECRET_PREFIX + name, "");
        if (!StringUtils.hasText(fallback)) {
            log.warn("Secret '{}' is not configured in application properties either.", name);
        }
        return fallback;
    }
}
