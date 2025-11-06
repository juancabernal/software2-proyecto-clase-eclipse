package co.edu.uco.ucochallenge.infrastructure.primary.web.controller.advice;

import java.util.Collection;
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
import co.edu.uco.ucochallenge.crosscutting.dto.MessageDTO;
import co.edu.uco.ucochallenge.crosscutting.legacy.exception.BusinessException;
import co.edu.uco.ucochallenge.crosscutting.legacy.exception.DomainValidationException;
import co.edu.uco.ucochallenge.crosscutting.legacy.exception.NotFoundException;
import co.edu.uco.ucochallenge.crosscutting.legacy.exception.NotificationDeliveryException;
import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.cache.catalog.MessagesCatalogCache;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class PrimaryGlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(PrimaryGlobalExceptionHandler.class);

    private static final String GENERIC_ERROR_MESSAGE =
            "Ocurrió un error inesperado. Por favor, intente nuevamente más tarde.";
    private static final String GENERIC_TECHNICAL_MESSAGE =
            "No se recibieron detalles técnicos adicionales para diagnosticar el error.";

    private final MessagesCatalogCache messagesCatalogCache;

    public PrimaryGlobalExceptionHandler(final MessagesCatalogCache messagesCatalogCache) {
        this.messagesCatalogCache = messagesCatalogCache;
    }

    /** Obtiene el mensaje completo (usuario + técnico) desde el catálogo con fallbacks seguros. */
    private MessageDTO resolveCatalogMessage(final String code, final Throwable origin, final String userFallback) {
        final String safeUserFallback = Optional.ofNullable(userFallback)
                .filter(f -> !f.isBlank())
                .orElse(GENERIC_ERROR_MESSAGE);
        final String safeTechnicalFallback = Optional.ofNullable(origin)
                .map(this::technicalDetails)
                .filter(f -> !f.isBlank())
                .orElse(GENERIC_TECHNICAL_MESSAGE);

        if (code == null || code.isBlank()) {
            return buildMessage(null, null, safeUserFallback, safeTechnicalFallback);
        }

        try {
            final MessageDTO catalogMessage = messagesCatalogCache.getMessageSync(code);
            return buildMessage(catalogMessage, code, safeUserFallback, safeTechnicalFallback);
        } catch (Exception ex) {
            log.warn("No se pudo obtener el mensaje para el código '{}': {}", code, ex.toString());
            final String technical = safeTechnicalFallback + " | lookupFailure=" + ex.getClass().getSimpleName();
            return buildMessage(null, code, safeUserFallback, technical);
        }
    }

    private MessageDTO resolveCatalogMessage(final String code, final Throwable origin) {
        return resolveCatalogMessage(code, origin, GENERIC_ERROR_MESSAGE);
    }

    private MessageDTO resolveCatalogMessage(final String code) {
        return resolveCatalogMessage(code, null, GENERIC_ERROR_MESSAGE);
    }

    private MessageDTO buildMessage(final MessageDTO source,
            final String requestedCode,
            final String userFallback,
            final String technicalFallback) {

        final String resolvedCode = firstNonBlank(
                Optional.ofNullable(source).map(MessageDTO::getCode).orElse(null),
                requestedCode);

        final MessageDTO result = new MessageDTO();
        result.setCode(resolvedCode);
        if (source != null) {
            result.setGeneralMessage(source.getGeneralMessage());
        }

        final String resolvedUser = firstNonBlank(
                Optional.ofNullable(source).map(MessageDTO::getUserMessage).orElse(null),
                Optional.ofNullable(source).map(MessageDTO::getGeneralMessage).orElse(null),
                userFallback,
                GENERIC_ERROR_MESSAGE,
                resolvedCode);
        result.setUserMessage(resolvedUser);

        final String resolvedTechnical = firstNonBlank(
                Optional.ofNullable(source).map(MessageDTO::getTechnicalMessage).orElse(null),
                technicalFallback,
                resolvedUser,
                resolvedCode,
                GENERIC_TECHNICAL_MESSAGE);
        result.setTechnicalMessage(resolvedTechnical);

        return result;
    }

    private String firstNonBlank(final String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private String technicalDetails(final Throwable throwable) {
        final StringBuilder details = new StringBuilder();
        Throwable current = throwable;
        while (current != null) {
            if (details.length() > 0) {
                details.append(" -> ");
            }
            details.append(current.getClass().getName());
            final String message = current.getMessage();
            if (message != null && !message.isBlank()) {
                details.append(": ").append(message.trim());
            }
            current = current.getCause();
        }
        return details.toString();
    }

    // ================== EXCEPCIONES DE NEGOCIO ==================
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(final BusinessException ex) {
        final MessageDTO message = resolveCatalogMessage(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.businessError(message.getCode(), message));
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleDomainValidation(final DomainValidationException ex) {
        final MessageDTO message = resolveCatalogMessage(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.validationError(message.getCode(), message));
    }

    // ================== RECURSOS NO ENCONTRADOS ==================
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(final NotFoundException ex) {
        final MessageDTO message = resolveCatalogMessage(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.businessError(message.getCode(), message));
    }

    // ================== VALIDACIONES (Jakarta / Spring) ==================
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleJakartaConstraint(final ConstraintViolationException ex) {
        final MessageDTO message = Optional.ofNullable(ex.getConstraintViolations())
                .stream()
                .flatMap(Collection::stream)
                .findFirst()
                .map(violation -> {
                    final String violationCode = Optional.ofNullable(violation.getMessage())
                            .filter(c -> !c.isBlank())
                            .orElse("validation.constraint.violation");
                    final String userFallback = String.format(
                            "El valor del campo '%s' no es válido.", violation.getPropertyPath());
                    final String technicalDetail = String.format(
                            "Constraint on '%s' rejected value '%s'",
                            violation.getPropertyPath(), violation.getInvalidValue());
                    final Throwable detail = new IllegalArgumentException(technicalDetail, ex);
                    return resolveCatalogMessage(violationCode, detail, userFallback);
                })
                .orElseGet(() -> resolveCatalogMessage("validation.constraint.violation", ex));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.validationError(message.getCode(), message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex) {
        final MessageDTO message = Optional.ofNullable(ex.getBindingResult().getFieldError())
                .map(fieldError -> {
                    final String code = Optional.ofNullable(fieldError.getDefaultMessage())
                            .filter(c -> !c.isBlank())
                            .orElse("validation.method.argument");
                    final String userFallback = String.format("El campo '%s' no es válido.", fieldError.getField());
                    final String detail = String.format(
                            "Field '%s' rejected value '%s' (binding failure)",
                            fieldError.getField(), fieldError.getRejectedValue());
                    return resolveCatalogMessage(code, new IllegalArgumentException(detail, ex), userFallback);
                })
                .orElseGet(() -> resolveCatalogMessage("validation.method.argument", ex));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.validationError(message.getCode(), message));
    }

    @ExceptionHandler(NotificationDeliveryException.class)
    public ResponseEntity<ApiErrorResponse> handleNotificationDelivery(final NotificationDeliveryException ex) {
        final MessageDTO message = resolveCatalogMessage(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiErrorResponse.unexpectedError(message.getCode(), message));
    }

    // ================== ERRORES DE BASE DE DATOS ==================
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(final DataIntegrityViolationException ex) {
        final MessageDTO message = resolveCatalogMessage("exception.general.technical", ex,
                "Se detectó una inconsistencia con los datos almacenados.");
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.businessError(message.getCode(), message));
    }

    // ================== ENTIDAD RELACIONADA NO ENCONTRADA ==================
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFound(final EntityNotFoundException ex) {
        final MessageDTO message = resolveCatalogMessage("exception.general.validation", ex,
                "La información relacionada no existe o fue eliminada.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.businessError(message.getCode(), message));
    }

    // ================== ERROR DESCONOCIDO ==================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(final Exception ex) {
        log.error("Error inesperado: ", ex);
        final MessageDTO message = resolveCatalogMessage("exception.general.unexpected", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.unexpectedError(message.getCode(), message));
    }
}
