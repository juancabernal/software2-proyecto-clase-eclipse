package co.edu.uco.api_gateway.controllers.handler;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import co.edu.uco.api_gateway.dto.ApiErrorResponse;
import co.edu.uco.api_gateway.exception.DownstreamException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DownstreamException.class)
    public ResponseEntity<ApiErrorResponse> handleDownstreamException(final DownstreamException exception) {
        final HttpStatusCode status = Optional.ofNullable(exception.getStatus())
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
        ApiErrorResponse error = Optional.ofNullable(exception.getError())
                .orElse(ApiErrorResponse.of(status.value(), "UPSTREAM_ERROR", "Unknown downstream error"));

        // 🧠 Intentamos extraer un JSON embebido en el technicalMessage tipo "body: {...}"
        final String techMsg = error.technicalMessage();
        if (techMsg != null && techMsg.contains("body:")) {
            try {
                int start = techMsg.indexOf("{", techMsg.indexOf("body:"));
                int end = techMsg.lastIndexOf("}") + 1;
                if (start > -1 && end > start) {
                    String jsonPart = techMsg.substring(start, end);
                    // Parseamos el JSON interno
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(jsonPart);

                    String userMessage = node.has("userMessage")
                            ? node.get("userMessage").asText()
                            : error.userMessage();
                    String technicalMessage = node.has("technicalMessage")
                            ? node.get("technicalMessage").asText()
                            : error.technicalMessage();

                    error = ApiErrorResponse.of(status.value(), userMessage, technicalMessage);
                }
            } catch (Exception e) {
                LOGGER.warn("Could not parse downstream body JSON", e);
            }
        }

        LOGGER.warn("Downstream request failed with status {} and user message {}", status, error.userMessage());
        return ResponseEntity.status(status).body(error);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(final Exception exception) {
        LOGGER.error("Unexpected error in API Gateway", exception);
        final ApiErrorResponse error = ApiErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                exception.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
