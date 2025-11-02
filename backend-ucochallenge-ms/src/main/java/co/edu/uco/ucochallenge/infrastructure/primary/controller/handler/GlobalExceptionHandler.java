package co.edu.uco.ucochallenge.infrastructure.primary.controller.handler;

import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import co.edu.uco.ucochallenge.crosscuting.exception.ApplicationException;
import co.edu.uco.ucochallenge.crosscuting.exception.DomainException;
import co.edu.uco.ucochallenge.crosscuting.exception.InfrastructureException;
import co.edu.uco.ucochallenge.crosscuting.exception.UcoChallengeException;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageCodes;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageProvider;
import co.edu.uco.ucochallenge.infrastructure.primary.controller.response.ApiErrorResponse;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

@ControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(DomainException.class)
        public ResponseEntity<ApiErrorResponse> handleDomainException(final DomainException exception) {
                LOGGER.warn(exception.getTechnicalMessage(), exception);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiErrorResponse.of(HttpStatus.BAD_REQUEST.value(), exception.getUserMessage(),
                                                exception.getTechnicalMessage()));
        }

        @ExceptionHandler(ApplicationException.class)
        public ResponseEntity<ApiErrorResponse> handleApplicationException(final ApplicationException exception) {
                LOGGER.error(exception.getTechnicalMessage(), exception);
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                                .body(ApiErrorResponse.of(HttpStatus.UNPROCESSABLE_ENTITY.value(), exception.getUserMessage(),
                                                exception.getTechnicalMessage()));
        }

        @ExceptionHandler(InfrastructureException.class)
        public ResponseEntity<ApiErrorResponse> handleInfrastructureException(final InfrastructureException exception) {
                LOGGER.error(exception.getTechnicalMessage(), exception);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                exception.getUserMessage(), exception.getTechnicalMessage()));
        }

        @ExceptionHandler(UcoChallengeException.class)
        public ResponseEntity<ApiErrorResponse> handleGenericUcoChallengeException(final UcoChallengeException exception) {
                LOGGER.error(exception.getTechnicalMessage(), exception);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                exception.getUserMessage(), exception.getTechnicalMessage()));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponse> handleUnexpectedException(final Exception exception) {
                final String technicalMessage = MessageProvider
                                .getMessage(MessageCodes.Application.UNEXPECTED_ERROR_TECHNICAL);
                LOGGER.error(technicalMessage, exception);
                final String userMessage = MessageProvider
                                .getMessage(MessageCodes.Application.UNEXPECTED_ERROR_USER);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), userMessage,
                                                exception.getMessage()));
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiErrorResponse> handleInvalidPayload(final HttpMessageNotReadableException exception) {
                LOGGER.warn("Invalid request payload", exception);

                String userMessage = "El cuerpo de la solicitud tiene datos con formato inválido.";
                final Throwable cause = exception.getMostSpecificCause();
                if (cause instanceof InvalidFormatException invalidFormat) {
                        final String fields = invalidFormat.getPath().stream()
                                        .map(reference -> reference.getFieldName())
                                        .filter(Objects::nonNull)
                                        .distinct()
                                        .collect(Collectors.joining(", "));
                        if (!fields.isBlank()) {
                                userMessage = String.format("Los campos %s deben tener un formato válido (UUID si aplica).", fields);
                        }
                }

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiErrorResponse.of(HttpStatus.BAD_REQUEST.value(), userMessage,
                                                exception.getMostSpecificCause() != null
                                                                ? exception.getMostSpecificCause().getMessage()
                                                                : exception.getMessage()));
        }
}
