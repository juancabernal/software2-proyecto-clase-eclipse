package co.edu.uco.ucochallenge.infrastructure.primary.controller.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import co.edu.uco.ucochallenge.crosscuting.exception.DomainException;
import co.edu.uco.ucochallenge.crosscuting.key.MessageKey;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageCodes;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageServicePortHolder;
import co.edu.uco.ucochallenge.infrastructure.primary.controller.response.ApiErrorResponse;
import co.edu.uco.ucochallenge.shared.InMemoryMessageServicePort;

class GlobalExceptionHandlerTest {

    private static final Map<String, String> MESSAGES = Map.ofEntries(
            Map.entry(MessageCodes.Application.UNEXPECTED_ERROR_TECHNICAL, "UNEXPECTED_ERROR - Revisa trazas y causa raíz."),
            Map.entry(MessageCodes.Application.UNEXPECTED_ERROR_USER, "Se presentó un error inesperado. Intenta nuevamente."),
            Map.entry(MessageKey.RequestPayload.INVALID, "El cuerpo de la solicitud tiene datos con formato inválido."),
            Map.entry(MessageKey.RequestPayload.INVALID_FIELDS,
                    "Los campos {fields} deben tener un formato válido (UUID si aplica)."),
            Map.entry(MessageKey.RequestPayload.TECHNICAL, "INVALID_REQUEST_PAYLOAD"));

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        MessageServicePortHolder.configure(new InMemoryMessageServicePort(MESSAGES));
        handler = new GlobalExceptionHandler();
    }

    @AfterEach
    void tearDown() {
        MessageServicePortHolder.configure(null);
    }

    @Test
    void handleDomainExceptionReturnsNotFoundForNotFoundMessages() {
        final DomainException exception = DomainException.build("USER_NOT_FOUND", "Usuario no encontrado");

        final ResponseEntity<ApiErrorResponse> response = handler.handleDomainException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("USER_NOT_FOUND", response.getBody().technicalMessage());
    }

    @Test
    void handleDomainExceptionReturnsConflictForDuplicateMessages() {
        final DomainException exception = DomainException.build("EMAIL_ALREADY_REGISTERED", "Duplicado");

        final ResponseEntity<ApiErrorResponse> response = handler.handleDomainException(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("EMAIL_ALREADY_REGISTERED", response.getBody().technicalMessage());
    }

    @Test
    void handleDomainExceptionReturnsBadRequestByDefault() {
        final DomainException exception = DomainException.build("INVALID_DATA", "Datos inválidos");

        final ResponseEntity<ApiErrorResponse> response = handler.handleDomainException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_DATA", response.getBody().technicalMessage());
    }

    @Test
    void handleUnexpectedExceptionUsesCatalogMessages() {
        final RuntimeException exception = new RuntimeException("should-not-leak");

        final ResponseEntity<ApiErrorResponse> response = handler.handleUnexpectedException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UNEXPECTED_ERROR - Revisa trazas y causa raíz.", response.getBody().technicalMessage());
        assertEquals("Se presentó un error inesperado. Intenta nuevamente.", response.getBody().userMessage());
    }

    @Test
    void handleInvalidPayloadUsesCatalogMessages() {
        final InvalidFormatException cause = new InvalidFormatException(null, "Invalid", "value", UUID.class);
        cause.prependPath(new JsonMappingException.Reference(GlobalExceptionHandlerTest.class, "userId"));
        final HttpMessageNotReadableException exception = new HttpMessageNotReadableException("invalid", cause, null);

        final ResponseEntity<ApiErrorResponse> response = handler.handleInvalidPayload(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Los campos userId deben tener un formato válido (UUID si aplica).",
                response.getBody().userMessage());
        assertEquals("INVALID_REQUEST_PAYLOAD", response.getBody().technicalMessage());
    }
}
