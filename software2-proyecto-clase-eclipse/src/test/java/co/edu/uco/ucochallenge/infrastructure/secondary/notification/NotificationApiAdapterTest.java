package co.edu.uco.ucochallenge.infrastructure.secondary.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.uco.ucochallenge.domain.notification.model.NotificationMessage;
import co.edu.uco.ucochallenge.domain.notification.model.NotificationMessage.Person;
import co.edu.uco.ucochallenge.domain.notification.model.NotificationMessage.Recipient;
import co.edu.uco.ucochallenge.domain.secret.port.SecretProviderPort;
import co.edu.uco.ucochallenge.infrastructure.secondary.notification.config.NotificationApiProperties;

@ExtendWith(MockitoExtension.class)
class NotificationApiAdapterTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SecretProviderPort secretProviderPort;

    private NotificationApiProperties properties;

    private NotificationApiAdapter adapter;

    @BeforeEach
    void setUp() {
        properties = new NotificationApiProperties();
        properties.setBaseUrl("https://notification.example");
        properties.setDuplicatePath("/sender");
        adapter = new NotificationApiAdapter(restTemplate, properties, secretProviderPort);
    }

    @Test
    void shouldSendNotificationWithMappedPayload() throws Exception {
        when(secretProviderPort.getSecret("cliente-id")).thenReturn("client-123");
        when(secretProviderPort.getSecret("cliente-secret-id")).thenReturn("secret-xyz");

        final ResponseEntity<String> response = ResponseEntity.ok("accepted");
        final ArgumentCaptor<HttpEntity<String>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        when(restTemplate.postForEntity(any(URI.class), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        final NotificationMessage message = new NotificationMessage(
                NotificationMessage.NotificationEvent.DUPLICATE_EMAIL,
                "Duplicate detected",
                "Hola Test",
                Instant.parse("2024-01-01T10:15:30Z"),
                Person.empty(),
                new Person("Test User", "user@example.com", "3001234567"),
                List.of(new Recipient("USER", "Test User", "user@example.com", "3001234567")),
                "duplicate_alert",
                "EMAIL");

        adapter.send(message);

        verify(restTemplate).postForEntity(eq(URI.create("https://notification.example/client-123/sender")),
                entityCaptor.capture(), eq(String.class));

        final HttpEntity<String> captured = entityCaptor.getValue();
        assertThat(captured.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        final String expectedAuth = "Basic "
                + Base64.getEncoder().encodeToString("client-123:secret-xyz".getBytes(StandardCharsets.UTF_8));
        assertThat(captured.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo(expectedAuth);

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode body = mapper.readTree(captured.getBody());
        assertThat(body.get("notificationId").asText()).isEqualTo("duplicate_alert");
        assertThat(body.get("templateId").asText()).isEqualTo("predeterminado");
        final JsonNode userNode = body.get("user");
        assertThat(userNode.get("email").asText()).isEqualTo("user@example.com");
        assertThat(userNode.get("number").asText()).isEqualTo("+573001234567");
        assertThat(userNode.get("id").asText()).isEqualTo("user@example.com");
    }

    @Test
    void shouldSkipWhenBaseUrlIsMissing() {
        properties.setBaseUrl("   ");

        final NotificationMessage message = new NotificationMessage(
                NotificationMessage.NotificationEvent.DUPLICATE_EMAIL,
                "Duplicate detected",
                "Hola Test",
                Instant.now(),
                Person.empty(),
                new Person("Test User", "user@example.com", "3001234567"),
                List.of(new Recipient("USER", "Test User", "user@example.com", "3001234567")),
                "duplicate_alert",
                "EMAIL");

        adapter.send(message);

        verifyNoInteractions(restTemplate);
        verify(secretProviderPort, never()).getSecret("cliente-id");
    }
}
