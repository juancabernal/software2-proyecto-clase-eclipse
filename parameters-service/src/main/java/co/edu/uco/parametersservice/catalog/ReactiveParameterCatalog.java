package co.edu.uco.parametersservice.catalog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * Catálogo reactivo en memoria que gestiona los parámetros y emite eventos
 * cuando cambian.
 */
@Component
public class ReactiveParameterCatalog {

    private final Map<String, Parameter> parameters = new ConcurrentHashMap<>();
    private final Sinks.Many<ParameterChange> sink = Sinks.many().replay().latest();
    private final Flux<ParameterChange> changeStream = sink.asFlux();

    public ReactiveParameterCatalog() {
        loadDefaults();
    }

    public Flux<Parameter> findAll() {
        return Flux.defer(() -> Flux.fromIterable(parameters.values()).map(this::copyOf));
    }

    public Mono<Parameter> findByKey(String key) {
        return Mono.defer(() -> Mono.justOrEmpty(parameters.get(key)).map(this::copyOf));
    }

    public Mono<Parameter> save(Parameter parameter) {
        return Mono.fromSupplier(() -> {
            Parameter sanitized = copyOf(parameter);
            CatalogEventType type = parameters.containsKey(sanitized.getKey()) ? CatalogEventType.UPDATED
                    : CatalogEventType.CREATED;
            parameters.put(sanitized.getKey(), sanitized);
            emit(type, sanitized);
            return copyOf(sanitized);
        });
    }

    public Mono<Parameter> remove(String key) {
        return Mono.defer(() -> {
            Parameter removed = parameters.remove(key);
            if (removed == null) {
                return Mono.empty();
            }
            emit(CatalogEventType.DELETED, removed);
            return Mono.just(copyOf(removed));
        });
    }

    public Flux<ParameterChange> changes() {
        return changeStream;
    }

    private void emit(CatalogEventType type, Parameter parameter) {
        sink.emitNext(new ParameterChange(type, copyOf(parameter)), Sinks.EmitFailureHandler.FAIL_FAST);
    }

    private Parameter copyOf(Parameter parameter) {
        return new Parameter(parameter.getKey(), parameter.getValue());
    }

    private void register(String key, String value) {
        parameters.put(key, new Parameter(key, value));
    }

    private void loadDefaults() {
        register("notification.admin.email", "juan.bernal8928@uco.edu.co");
        register("notification.duplicated.email.template",
                "Hola %s, detectamos un intento de registro con su correo electrónico. Si no ha sido usted, por favor contacte al administrador.");
        register("notification.duplicated.mobile.template",
                "Hola %s, detectamos un intento de registro con su número móvil. Si no ha sido usted, comuníquese con soporte.");
        register("notification.confirmation.email.strategy", "ENVIAR_LINK_CONFIRMACION");
        register("notification.confirmation.mobile.strategy", "ENVIAR_CODIGO_SMS");
        register("notification.email.maxRetries", "3");
        register("validation.code.timeExpiration", "5");
        register("verification.code.expiration.minutes", "5");  // tiempo de vida del token
        register("verification.code.max.attempts", "3");         // número máximo de intentos


        register("user.idNumber.minLength", "5");
        register("user.idNumber.maxLength", "20");
        register("user.name.minLength", "2");
        register("user.name.maxLength", "40");
        register("user.email.minLength", "10");
        register("user.email.maxLength", "100");
        register("user.mobile.length", "10");
        register("user.name.pattern", "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$");
        register("user.idNumber.pattern", "^\\d+$");
        register("user.email.pattern", "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        register("user.mobile.pattern", "^\\d{10}$");
        register("validation.code.maxAttempts", "3");
    }
}
