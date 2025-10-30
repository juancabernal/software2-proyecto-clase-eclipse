package co.edu.uco.messageservice.catalog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * Catálogo reactivo en memoria que mantiene los mensajes y emite eventos cada
 * vez que cambia su contenido.
 */
@Component
public class ReactiveMessageCatalog {

    private final Map<String, Message> messages = new ConcurrentHashMap<>();
    private final Sinks.Many<MessageChange> sink = Sinks.many().replay().latest();
    private final Flux<MessageChange> changeStream = sink.asFlux();

    public ReactiveMessageCatalog() {
        loadDefaults();
    }

    public Flux<Message> findAll() {
        return Flux.defer(() -> Flux.fromIterable(messages.values()).map(this::copyOf));
    }

    public Mono<Message> findByKey(String key) {
        return Mono.defer(() -> Mono.justOrEmpty(messages.get(key)).map(this::copyOf));
    }

    public Mono<Message> save(Message message) {
        return Mono.fromSupplier(() -> {
            Message sanitized = copyOf(message);
            CatalogEventType type = messages.containsKey(sanitized.getKey()) ? CatalogEventType.UPDATED
                    : CatalogEventType.CREATED;
            messages.put(sanitized.getKey(), sanitized);
            emit(type, sanitized);
            return copyOf(sanitized);
        });
    }

    public Mono<Message> remove(String key) {
        return Mono.defer(() -> {
            Message removed = messages.remove(key);
            if (removed == null) {
                return Mono.empty();
            }
            emit(CatalogEventType.DELETED, removed);
            return Mono.just(copyOf(removed));
        });
    }

    public Flux<MessageChange> changes() {
        return changeStream;
    }

    private void emit(CatalogEventType type, Message message) {
        sink.emitNext(new MessageChange(type, copyOf(message)), Sinks.EmitFailureHandler.FAIL_FAST);
    }

    private Message copyOf(Message message) {
        return new Message(message.getKey(), message.getValue());
    }

    private void register(String key, String value) {
        messages.put(key, new Message(key, value));
    }

    private void loadDefaults() {
        register("exception.general.unexpected",
                "Ha ocurrido un error inesperado. Por favor, intente nuevamente más tarde.");
        register("exception.general.technical", "Se produjo un error interno al procesar la solicitud.");
        register("exception.general.user", "No fue posible procesar la solicitud con la información recibida.");

        register("register.user.success", "Usuario registrado correctamente.");
        register("register.user.validation.idType.required", "El tipo de identificación es obligatorio.");
        register("register.user.validation.idType.notFound",
                "El tipo de identificación indicado no existe en el sistema.");
        register("register.user.validation.idNumber.required", "El número de identificación es obligatorio.");
        register("register.user.validation.idNumber.invalidFormat",
                "El número de identificación solo puede contener dígitos.");
        register("register.user.validation.idNumber.length",
                "El número de identificación debe tener entre 5 y 20 dígitos.");
        register("register.user.validation.name.required", "El nombre suministrado es obligatorio.");
        register("register.user.validation.name.invalidCharacters", "El nombre solo puede contener letras y espacios.");
        register("register.user.validation.name.length", "El nombre debe tener entre 2 y 40 caracteres.");
        register("register.user.validation.homeCity.required", "La ciudad de residencia es obligatoria.");
        register("register.user.validation.homeCity.notFound",
                "La ciudad de residencia indicada no existe en el sistema.");
        register("register.user.validation.email.required", "El correo electrónico es obligatorio.");
        register("register.user.validation.email.length",
                "El correo electrónico debe tener entre 10 y 100 caracteres.");
        register("register.user.validation.email.invalidFormat", "El formato del correo electrónico no es válido.");
        register("register.user.validation.mobile.required", "El número de teléfono móvil es obligatorio.");
        register("register.user.validation.mobile.invalidFormat",
                "El número de teléfono móvil debe contener exactamente 10 dígitos.");

        register("register.user.rule.id.duplicated",
                "Se detectó un conflicto con el identificador del usuario; se generará uno nuevo.");
        register("register.user.rule.idTypeNumber.duplicated.admin",
                "Existe un usuario con el mismo tipo y número de identificación. Se notificará al administrador.");
        register("register.user.rule.idTypeNumber.duplicated.executor",
                "Ya existe un usuario registrado con ese tipo y número de identificación.");
        register("register.user.rule.email.duplicated.owner",
                "El correo electrónico suministrado ya está registrado; se notificará al propietario.");
        register("register.user.rule.email.duplicated.executor",
                "Ya existe un usuario con el correo electrónico proporcionado.");
        register("register.user.rule.mobile.duplicated.owner",
                "El número de teléfono suministrado ya está registrado; se notificará al propietario.");
        register("register.user.rule.mobile.duplicated.executor",
                "Ya existe un usuario con el número de teléfono proporcionado.");
        register("register.user.rule.email.confirmation.strategy",
                "Se enviará la estrategia de confirmación del correo electrónico.");
        register("register.user.rule.email.confirmation.pending",
                "El correo electrónico debe ser confirmado para finalizar el registro.");
        register("register.user.rule.mobile.confirmation.strategy",
                "Se enviará la estrategia de confirmación del número móvil.");
        register("register.user.rule.mobile.confirmation.pending",
                "El número móvil debe ser confirmado para finalizar el registro.");
        register("list.users.validation.page.negative", "La página solicitada no puede ser negativa.");
        register("list.users.validation.size.invalid", "El tamaño de página debe estar entre 1 y 50 registros.");

        register("application.unexpectedError.technical", "Ocurrió un error inesperado al procesar la solicitud.");
        register("application.unexpectedError.user",
                "No pudimos procesar tu solicitud en este momento. Por favor, inténtalo más tarde.");

        register("infrastructure.messageService.unavailable.technical",
                "El servicio de mensajes no está disponible en este momento.");
        register("infrastructure.messageService.unavailable.user",
                "No fue posible recuperar los mensajes solicitados. Inténtalo de nuevo más tarde.");
        register("infrastructure.parameterService.unavailable.technical",
                "El servicio de parámetros no está disponible en este momento.");
        register("infrastructure.parameterService.unavailable.user",
                "No fue posible recuperar los parámetros de validación. Inténtalo de nuevo más tarde.");
        register("infrastructure.parameterService.invalidResponse.technical",
                "Se recibió una respuesta inválida del servicio de parámetros.");
        register("infrastructure.parameterService.invalidResponse.user",
                "No fue posible procesar la configuración solicitada. Inténtalo nuevamente más tarde.");

        register("domain.user.idType.mandatory.technical", "El tipo de identificación del usuario es obligatorio.");
        register("domain.user.idType.mandatory.user", "Debe seleccionar un tipo de identificación.");
        register("domain.user.idNumber.empty.technical", "El número de identificación del usuario es obligatorio.");
        register("domain.user.idNumber.empty.user", "Debe ingresar el número de identificación.");
        register("domain.user.idNumber.invalidChars.technical",
                "El número de identificación solo puede contener caracteres permitidos.");
        register("domain.user.idNumber.invalidChars.user",
                "El número de identificación solo puede contener números.");
        register("domain.user.idNumber.length.technical",
                "El número de identificación debe tener entre {minLength} y {maxLength} caracteres.");
        register("domain.user.idNumber.length.user",
                "El número de identificación debe tener entre {minLength} y {maxLength} caracteres.");
        register("domain.user.field.mandatory.technical",
                "El campo '{fieldName}' del usuario es obligatorio.");
        register("domain.user.field.mandatory.user", "Debe diligenciar el campo {fieldName}.");
        register("domain.user.field.invalidChars.technical",
                "El campo '{fieldName}' contiene caracteres no permitidos.");
        register("domain.user.field.invalidChars.user", "El {fieldName} contiene caracteres no válidos.");
        register("domain.user.field.length.technical",
                "El campo '{fieldName}' debe tener entre {minLength} y {maxLength} caracteres.");
        register("domain.user.field.length.user",
                "El {fieldName} debe tener entre {minLength} y {maxLength} caracteres.");
        register("domain.user.homeCity.mandatory.technical", "La ciudad de residencia del usuario es obligatoria.");
        register("domain.user.homeCity.mandatory.user", "Debe seleccionar una ciudad de residencia.");
        register("domain.user.email.empty.technical", "El correo electrónico del usuario es obligatorio.");
        register("domain.user.email.empty.user", "Debe ingresar un correo electrónico.");
        register("domain.user.email.length.technical",
                "El correo electrónico debe tener entre {minLength} y {maxLength} caracteres.");
        register("domain.user.email.length.user",
                "El correo electrónico debe tener entre {minLength} y {maxLength} caracteres.");
        register("domain.user.email.invalidFormat.technical", "El formato del correo electrónico no es válido.");
        register("domain.user.email.invalidFormat.user", "El correo electrónico ingresado no es válido.");
        register("domain.user.mobile.empty.technical", "El número de teléfono móvil del usuario es obligatorio.");
        register("domain.user.mobile.empty.user", "Debe ingresar un número de teléfono móvil.");
        register("domain.user.mobile.invalidFormat.technical",
                "El número de teléfono móvil debe tener exactamente {expectedLength} dígitos.");
        register("domain.user.mobile.invalidFormat.user",
                "El número de celular debe tener {expectedLength} dígitos.");
        register("domain.user.notFound.technical", "El usuario solicitado no existe en el sistema.");
        register("domain.user.notFound.user", "No encontramos un usuario con la información suministrada.");
        register("domain.user.email.alreadyRegistered.technical",
                "El correo electrónico ya se encuentra asociado a otro usuario.");
        register("domain.user.email.alreadyRegistered.user", "El correo electrónico ingresado ya está registrado.");
        register("domain.user.idNumber.alreadyRegistered.technical",
                "El número de identificación ya está asociado a otro usuario.");
        register("domain.user.idNumber.alreadyRegistered.user",
                "El número de identificación ingresado ya está registrado.");
        register("domain.user.mobile.alreadyRegistered.technical",
                "El número de teléfono móvil ya se encuentra asociado a otro usuario.");
        register("domain.user.mobile.alreadyRegistered.user",
                "El número de celular ingresado ya está registrado.");
        register("domain.user.idType.notFound.technical", "El tipo de identificación solicitado no existe.");
        register("domain.user.idType.notFound.user", "El tipo de identificación seleccionado no es válido.");
        register("domain.user.homeCity.notFound.technical", "La ciudad de residencia indicada no existe.");
        register("domain.user.homeCity.notFound.user", "La ciudad de residencia seleccionada no es válida.");
    }
}
