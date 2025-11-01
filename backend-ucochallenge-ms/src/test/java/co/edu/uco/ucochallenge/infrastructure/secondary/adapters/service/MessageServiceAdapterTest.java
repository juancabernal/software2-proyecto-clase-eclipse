package co.edu.uco.ucochallenge.infrastructure.secondary.adapters.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import co.edu.uco.ucochallenge.crosscuting.exception.InfrastructureException;
import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.service.orchestration.CatalogService;

class MessageServiceAdapterTest {

    private CatalogService catalogService;
    private MessageServiceAdapter adapter;

    @BeforeEach
    void setUp() {
        catalogService = mock(CatalogService.class);
        adapter = new MessageServiceAdapter(catalogService);
    }

    @Test
    void shouldFetchLatestValueOnEveryCall() {
        when(catalogService.findMessageValue(eq("greeting"), anyMap()))
                .thenReturn(Optional.of("Hola"), Optional.of("Adios"));

        final String first = adapter.getMessage("greeting");
        final String second = adapter.getMessage("greeting");

        assertThat(first).isEqualTo("Hola");
        assertThat(second).isEqualTo("Adios");
        verify(catalogService, times(2)).findMessageValue(eq("greeting"), anyMap());
    }

    @Test
    void shouldReturnKeyWhenCatalogDoesNotContainMessage() {
        when(catalogService.findMessageValue(eq("missing"), anyMap())).thenReturn(Optional.empty());

        final String value = adapter.getMessage("missing", Map.of("lang", "es"));

        assertThat(value).isEqualTo("missing");
    }

    @Test
    void shouldFailFastWhenCatalogThrowsException() {
        final WebClientResponseException error = WebClientResponseException.create(500, "error",
                HttpHeaders.EMPTY, new byte[0], null);
        when(catalogService.findMessageValue(eq("greeting"), anyMap())).thenThrow(error);

        assertThatThrownBy(() -> adapter.getMessage("greeting"))
                .isInstanceOf(InfrastructureException.class);
    }
}

