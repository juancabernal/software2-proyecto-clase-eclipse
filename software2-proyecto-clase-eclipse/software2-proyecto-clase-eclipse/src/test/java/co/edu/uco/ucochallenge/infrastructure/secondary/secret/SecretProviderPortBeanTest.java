package co.edu.uco.ucochallenge.infrastructure.secondary.secret;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import co.edu.uco.ucochallenge.domain.secret.port.SecretProviderPort;

@SpringBootTest(properties = "azure.keyvault.url=false")
class SecretProviderPortBeanTest {

    @Autowired
    private SecretProviderPort secretProvider;

    @Test
    void secretProviderBeanIsAvailable() {
        assertThat(secretProvider).isNotNull();
        assertThat(secretProvider.getSecret("db-username")).isNotBlank();
    }
}
