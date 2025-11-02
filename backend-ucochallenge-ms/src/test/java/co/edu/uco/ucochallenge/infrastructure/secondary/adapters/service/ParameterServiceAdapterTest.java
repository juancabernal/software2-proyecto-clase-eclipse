package co.edu.uco.ucochallenge.infrastructure.secondary.adapters.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import co.edu.uco.ucochallenge.crosscuting.exception.InfrastructureException;
import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.service.orchestration.CatalogService;

class ParameterServiceAdapterTest {

    private CatalogService catalogService;
    private ParameterServiceAdapter adapter;

    @BeforeEach
    void setUp() {
        catalogService = mock(CatalogService.class);
        adapter = new ParameterServiceAdapter(catalogService);
    }

    @Test
    void shouldFetchLatestValueOnEveryCall() {
        when(catalogService.findParameterValue("feature.flag"))
                .thenReturn(Optional.of("OFF"), Optional.of("ON"));

        final String first = adapter.getParameter("feature.flag");
        final String second = adapter.getParameter("feature.flag");

        assertThat(first).isEqualTo("OFF");
        assertThat(second).isEqualTo("ON");
        verify(catalogService, times(2)).findParameterValue("feature.flag");
    }

    @Test
    void shouldReturnEmptyValueWhenCatalogDoesNotContainParameter() {
        when(catalogService.findParameterValue("missing")).thenReturn(Optional.empty());

        final String value = adapter.getParameter("missing");

        assertThat(value).isEmpty();
    }

    @Test
    void shouldFailFastWhenCatalogThrowsException() {
        final WebClientResponseException error = WebClientResponseException.create(500, "error",
                HttpHeaders.EMPTY, new byte[0], null);
        when(catalogService.findParameterValue("feature.flag")).thenThrow(error);

        assertThatThrownBy(() -> adapter.getParameter("feature.flag"))
                .isInstanceOf(InfrastructureException.class);
    }
}

