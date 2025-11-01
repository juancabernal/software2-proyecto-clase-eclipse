package co.edu.uco.ucochallenge.infrastructure.primary.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import co.edu.uco.ucochallenge.domain.secret.port.SecretProviderPort;

@Component
public class StartupSecretLogger {

    private static final Logger log = LoggerFactory.getLogger(StartupSecretLogger.class);

    private final SecretProviderPort secretProvider;

    public StartupSecretLogger(final SecretProviderPort secretProvider) {
        this.secretProvider = secretProvider;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void testKeyVaultConnection() {
        try {
            String username = secretProvider.getSecret("db-username");
            log.info("✅ Successfully retrieved db-username from Azure Key Vault: {}", mask(username));
        } catch (Exception e) {
            log.warn("⚠️ Could not access Azure Key Vault: {}", e.getMessage());
        }
    }

    private String mask(final String value) {
        if (value == null || value.isBlank()) {
            return "(empty)";
        }
        int visible = Math.min(2, value.length());
        return "****" + value.substring(value.length() - visible);
    }
}
