package co.edu.uco.ucochallenge.infrastructure.secondary.adapters.service.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import co.edu.uco.ucochallenge.crosscuting.config.CatalogConfig;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

class MessageCatalogClientTest {

    private MockWebServer mockWebServer;
    private MessageCatalogClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        final String baseUrl = mockWebServer.url("/messages").toString();
        final WebClient webClient = new CatalogConfig().messageCatalogWebClient(WebClient.builder(), baseUrl);
        client = new MessageCatalogClient(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldFetchLatestValueEveryTime() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"key\":\"greeting\",\"value\":\"Hola\"}"));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"key\":\"greeting\",\"value\":\"Adios\"}"));

        final String firstValue = client.findValueByKey("greeting", Map.of("lang", "es"))
                .orElseThrow();
        final String secondValue = client.findValueByKey("greeting", Map.of("lang", "es"))
                .orElseThrow();

        assertThat(firstValue).isEqualTo("Hola");
        assertThat(secondValue).isEqualTo("Adios");

        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
        assertThat(mockWebServer.takeRequest().getHeader("Cache-Control"))
                .isEqualTo("no-cache, no-store, must-revalidate");
        assertThat(mockWebServer.takeRequest().getHeader("Pragma")).isEqualTo("no-cache");
    }

    @Test
    void shouldReturnEmptyWhenEntryIsMissing() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        assertThat(client.findValueByKey("missing", Map.of())).isEmpty();
    }
}
