package co.edu.uco.ucochallenge.infrastructure.secondary.adapters.service.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import co.edu.uco.ucochallenge.crosscuting.config.CatalogConfig;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

class ParameterCatalogClientTest {

    private MockWebServer mockWebServer;
    private ParameterCatalogClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        final String baseUrl = mockWebServer.url("/parameters").toString();
        final WebClient webClient = new CatalogConfig().parameterCatalogWebClient(WebClient.builder(), baseUrl);
        client = new ParameterCatalogClient(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldReturnLastValue() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"key\":\"feature.flag\",\"value\":\"OFF\"}"));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"key\":\"feature.flag\",\"value\":\"ON\"}"));

        final String firstValue = client.findValueByKey("feature.flag")
                .orElseThrow();
        final String secondValue = client.findValueByKey("feature.flag")
                .orElseThrow();

        assertThat(firstValue).isEqualTo("OFF");
        assertThat(secondValue).isEqualTo("ON");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
        assertThat(mockWebServer.takeRequest().getHeader("Cache-Control"))
                .isEqualTo("no-cache, no-store, must-revalidate");
        assertThat(mockWebServer.takeRequest().getHeader("Pragma")).isEqualTo("no-cache");
    }

    @Test
    void shouldBeEmptyWhenNotFound() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        assertThat(client.findValueByKey("missing"))
                .isEmpty();
    }
}
