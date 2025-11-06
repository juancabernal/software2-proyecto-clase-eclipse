package co.edu.uco.ucochallenge.infrastructure.primary.web.controller.advice;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import co.edu.uco.ucochallenge.application.ApiErrorResponse;
import co.edu.uco.ucochallenge.crosscutting.MessageCodes;
import co.edu.uco.ucochallenge.crosscutting.legacy.exception.BusinessException;
import co.edu.uco.ucochallenge.crosscutting.legacy.exception.DomainValidationException;
import co.edu.uco.ucochallenge.crosscutting.legacy.exception.NotFoundException;
import co.edu.uco.ucochallenge.crosscutting.legacy.exception.NotificationDeliveryException;
import co.edu.uco.ucochallenge.crosscutting.dto.MessageDTO;
import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.cache.catalog.MessagesCatalogCache;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class PrimaryGlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(PrimaryGlobalExceptionHandler.class);

    private static final String DEFAULT_USER_MESSAGE =
            "Ocurrió un error inesperado. Por favor, intente nuevamente más tarde.";
    private static final String DEFAULT_TECHNICAL_MESSAGE =
            "Message catalog lookup failed";

    private final MessagesCatalogCache messagesCatalogCache;

    public PrimaryGlobalExceptionHandler(final MessagesCatalogCache messagesCatalogCache) {
        this.messagesCatalogCache = messagesCatalogCache;
    }

    private record ResolvedMessage(
            String messageCode,
            String userMessage,
            String technicalMessage,
            String generalMessage) { }

    private ResolvedMessage resolveCatalogMessage(final String requestedCode) {
        final String code = normalizeCode(requestedCode);
        final String technicalFallback = String.format("%s para el código '%s'", DEFAULT_TECHNICAL_MESSAGE, code);

        try {
            Optional<MessageDTO> resolved = messagesCatalogCache
                    .getMessage(code)
                    .onErrorResume(e -> {
                        log.warn("Fallo consultando catálogo para '{}': {}", code, e.toString());
                        return reactor.core.publisher.Mono.empty();
                    })
                    .blockOptional();

            if (resolved.isPresent()) {
                final MessageDTO dto = resolved.get();
                final String userMessage = defaultIfBlank(dto.getUserMessageResolved(), DEFAULT_USER_MESSAGE);
                final String technicalMessage = defaultIfBlank(dto.getTechnicalMessageResolved(), technicalFallback);
                final String generalMessage = defaultIfBlank(dto.getGeneralMessageResolved(), userMessage);
                return new ResolvedMessage(code, userMessage, technicalMessage, generalMessage);
            }

        } catch (Exception ex) {
            log.warn("No se pudo obtener el mensaje para el código '{}': {}", code, ex.toString());
        }

        return new ResolvedMessage(code, DEFAULT_USER_MESSAGE, technicalFallback, DEFAULT_USER_MESSAGE);
    }

    private static String normalizeCode(final String requestedCode) {
        if (requestedCode == null || requestedCode.isBlank()) {
            return MessageCodes.EXCEPTION_GENERAL_UNEXPECTED;
        }
        return requestedCode.trim();
    }

    private static String defaultIfBlank(final String value, final String defaultValue) {
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    private static String extractCode(final RuntimeException ex, final String fallbackCode) {
        if (ex instanceof BusinessException business) {
            return normalizeCode(defaultIfBlank(business.getCode(), fallbackCode));
        }
        if (ex instanceof DomainValidationException validation) {
            return normalizeCode(defaultIfBlank(validation.getCode(), fallbackCode));
        }
        if (ex instanceof NotFoundException notFound) {
            return normalizeCode(defaultIfBlank(notFound.getCode(), fallbackCode));
        }
        if (ex instanceof NotificationDeliveryException notification) {
            return normalizeCode(defaultIfBlank(notification.getCode(), fallbackCode));
        }
        return normalizeCode(fallbackCode);
    }

    // ================== EXCEPCIONES DE NEGOCIO ==================
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(final BusinessException ex) {
        final ResolvedMessage resolved = resolveCatalogMessage(extractCode(ex, MessageCodes.EXCEPTION_GENERAL_VALIDATION));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.businessError(
                        resolved.messageCode(),
                        resolved.userMessage(),
                        resolved.technicalMessage(),
                        resolved.generalMessage()));
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleDomainValidation(final DomainValidationException ex) {
        final ResolvedMessage resolved = resolveCatalogMessage(extractCode(ex, MessageCodes.EXCEPTION_GENERAL_VALIDATION));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.validationError(
                        resolved.messageCode(),
                        resolved.userMessage(),
                        resolved.technicalMessage(),
                        resolved.generalMessage()));
    }

    // ================== RECURSOS NO ENCONTRADOS ==================
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(final NotFoundException ex) {
        final ResolvedMessage resolved = resolveCatalogMessage(extractCode(ex, MessageCodes.EXCEPTION_GENERAL_VALIDATION));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.businessError(
                        resolved.messageCode(),
                        resolved.userMessage(),
                        resolved.technicalMessage(),
                        resolved.generalMessage()));
    }

    // ================== VALIDACIONES (Jakarta / Spring) ==================
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleJakartaConstraint(final ConstraintViolationException ex) {
        final String code = ex.getConstraintViolations().stream()
                .map(v -> Optional.ofNullable(v.getMessage()).orElse(MessageCodes.EXCEPTION_GENERAL_VALIDATION))
                .findFirst()
                .orElse(MessageCodes.EXCEPTION_GENERAL_VALIDATION);
        final ResolvedMessage resolved = resolveCatalogMessage(code);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.validationError(
                        resolved.messageCode(),
                        resolved.userMessage(),
                        resolved.technicalMessage(),
                        resolved.generalMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex) {
        final String code = Optional.ofNullable(ex.getBindingResult().getFieldError())
                .map(FieldError::getDefaultMessage)
                .orElse(MessageCodes.EXCEPTION_GENERAL_VALIDATION);
        final ResolvedMessage resolved = resolveCatalogMessage(code);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.validationError(
                        resolved.messageCode(),
                        resolved.userMessage(),
                        resolved.technicalMessage(),
                        resolved.generalMessage()));
    }

    @ExceptionHandler(NotificationDeliveryException.class)
    public ResponseEntity<ApiErrorResponse> handleNotificationDelivery(final NotificationDeliveryException ex) {
        final ResolvedMessage resolved = resolveCatalogMessage(extractCode(ex, MessageCodes.VERIFICATION_NOTIFICATION_DELIVERY_FAILED));
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiErrorResponse.unexpectedError(
                        resolved.messageCode(),
                        resolved.userMessage(),
                        resolved.technicalMessage(),
                        resolved.generalMessage()));
    }

    // ================== ERRORES DE BASE DE DATOS ==================
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(final DataIntegrityViolationException ex) {
        final ResolvedMessage resolved = resolveCatalogMessage(MessageCodes.EXCEPTION_GENERAL_TECHNICAL);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.businessError(
                        resolved.messageCode(),
                        resolved.userMessage(),
                        resolved.technicalMessage(),
                        resolved.generalMessage()));
    }

    // ================== ENTIDAD RELACIONADA NO ENCONTRADA ==================
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFound(final EntityNotFoundException ex) {
        final ResolvedMessage resolved = resolveCatalogMessage(MessageCodes.EXCEPTION_GENERAL_VALIDATION);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.businessError(
                        resolved.messageCode(),
                        resolved.userMessage(),
                        resolved.technicalMessage(),
                        resolved.generalMessage()));
    }

    // ================== ERROR DESCONOCIDO ==================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(final Exception ex) {
        log.error("Error inesperado: ", ex);
        final ResolvedMessage resolved = resolveCatalogMessage(MessageCodes.EXCEPTION_GENERAL_UNEXPECTED);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.unexpectedError(
                        resolved.messageCode(),
                        resolved.userMessage(),
                        resolved.technicalMessage(),
                        resolved.generalMessage()));
    }
}
