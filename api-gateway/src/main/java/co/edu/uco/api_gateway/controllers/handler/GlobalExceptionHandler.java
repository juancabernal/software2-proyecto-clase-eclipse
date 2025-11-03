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
        final ApiErrorResponse error = Optional.ofNullable(exception.getError())
                .orElse(ApiErrorResponse.of(status.value(), status.toString(), status.toString()));

        LOGGER.warn("Downstream request failed with status {} and technical message {}", status, error.technicalMessage());
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
