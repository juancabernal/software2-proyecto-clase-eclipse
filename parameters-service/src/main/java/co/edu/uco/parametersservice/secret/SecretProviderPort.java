package co.edu.uco.parametersservice.secret;

@FunctionalInterface
public interface SecretProviderPort {

    String getSecret(String name);
}
