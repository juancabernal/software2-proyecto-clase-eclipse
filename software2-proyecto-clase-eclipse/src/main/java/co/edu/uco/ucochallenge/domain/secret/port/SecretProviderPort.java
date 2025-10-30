package co.edu.uco.ucochallenge.domain.secret.port;

/**
 * Domain port used to obtain sensitive configuration values.
 */
public interface SecretProviderPort {

    /**
     * Retrieves the value of a secret by its unique name. Implementations should
     * never throw when the secret is missing; instead, they must log a warning and
     * return an empty string so the caller can decide how to handle the
     * situation.
     *
     * @param name identifier of the secret to fetch.
     * @return resolved secret value, or an empty string when it cannot be
     *         determined.
     */
    String getSecret(String name);
}
