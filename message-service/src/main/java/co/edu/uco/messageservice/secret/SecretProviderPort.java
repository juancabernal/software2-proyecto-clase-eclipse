package co.edu.uco.messageservice.secret;

/**
 * Simple abstraction that allows the application to obtain secrets without
 * being tightly coupled to Azure Key Vault. This enables fallbacks during
 * local development while keeping credentials outside of the codebase.
 */
@FunctionalInterface
public interface SecretProviderPort {

    /**
     * Retrieve the value of the secret identified by {@code name}.
     *
     * @param name Secret identifier as stored in the external vault.
     * @return the secret value or an empty string when the secret is not
     *         available.
     */
    String getSecret(String name);
}
