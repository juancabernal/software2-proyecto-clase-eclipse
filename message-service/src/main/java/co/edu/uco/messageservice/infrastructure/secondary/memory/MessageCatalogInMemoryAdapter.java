package co.edu.uco.messageservice.infrastructure.secondary.memory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;

import co.edu.uco.messageservice.domain.event.MessageCatalogChangeEvent;
import co.edu.uco.messageservice.domain.event.MessageCatalogEventType;
import co.edu.uco.messageservice.domain.model.MessageDomainAggregate;
import co.edu.uco.messageservice.domain.port.MessageCatalogGateway;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * Secondary adapter that stores catalog messages in memory. It keeps the
 * existing behavior while exposing the operations through the domain port.
 */
@Component
public class MessageCatalogInMemoryAdapter implements MessageCatalogGateway {

    private final Map<String, MessageDomainAggregate> messages = new ConcurrentHashMap<>();
    private final Sinks.Many<MessageCatalogChangeEvent> sink = Sinks.many().replay().latest();
    private final Flux<MessageCatalogChangeEvent> changeStream = sink.asFlux();

    public MessageCatalogInMemoryAdapter() {
        loadDefaults();
    }

    @Override
    public Publisher<MessageDomainAggregate> fetchAll() {
        return Flux.defer(() -> Flux.fromIterable(messages.values()));
    }

    @Override
    public Publisher<MessageDomainAggregate> fetchByKey(String key) {
        return Mono.defer(() -> Mono.justOrEmpty(messages.get(key)));
    }

    @Override
    public Publisher<MessageDomainAggregate> save(MessageDomainAggregate message) {
        return Mono.fromSupplier(() -> {
            MessageDomainAggregate sanitized = MessageDomainAggregate.create(message.key(), message.value());
            MessageCatalogEventType type = messages.containsKey(sanitized.key()) ? MessageCatalogEventType.UPDATED
                    : MessageCatalogEventType.CREATED;
            messages.put(sanitized.key(), sanitized);
            emit(type, sanitized);
            return sanitized;
        });
    }

    @Override
    public Publisher<MessageDomainAggregate> deleteByKey(String key) {
        return Mono.defer(() -> {
            MessageDomainAggregate removed = messages.remove(key);
            if (removed == null) {
                return Mono.empty();
            }
            emit(MessageCatalogEventType.DELETED, removed);
            return Mono.just(removed);
        });
    }

    @Override
    public Publisher<MessageCatalogChangeEvent> listenChanges() {
        return changeStream;
    }

    private void emit(MessageCatalogEventType type, MessageDomainAggregate message) {
        sink.emitNext(new MessageCatalogChangeEvent(type, message), Sinks.EmitFailureHandler.FAIL_FAST);
    }

    private void register(String key, String value) {
        messages.put(key, MessageDomainAggregate.create(key, value));
    }

    private void loadDefaults() {
        register("exception.general.unexpected",
                "Ha ocurrido un error inesperado. Por favor, intente nuevamente más tarde.");
        register("exception.general.technical", "Se produjo un error interno al procesar la solicitud.");
        register("exception.general.user", "No fue posible procesar la solicitud con la información recibida.");

        register("register.user.success", "Usuario registrado exitosamente.");
        register("list.users.success", "Usuarios obtenidos exitosamente.");
        register("get.user.success", "Usuario obtenido exitosamente.");
        register("search.users.success", "Usuarios filtrados exitosamente.");
        register("delete.user.success", "Usuario eliminado exitosamente.");
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

        register("application.unexpectedError.user", "Se presentó un error inesperado. Intenta nuevamente.");
        register("application.unexpectedError.technical", "UNEXPECTED_ERROR - Revisa trazas y causa raíz.");

        register("infrastructure.messageService.unavailable.user", "El servicio de mensajes no está disponible.");
        register("infrastructure.messageService.unavailable.technical", "MESSAGE_SERVICE_UNAVAILABLE");
        register("infrastructure.parameterService.unavailable.user", "El servicio de parámetros no está disponible.");
        register("infrastructure.parameterService.unavailable.technical", "PARAMETER_SERVICE_UNAVAILABLE");
        register("infrastructure.parameterService.invalidResponse.user", "Valor de parámetro inválido.");
        register("infrastructure.parameterService.invalidResponse.technical", "PARAMETER_INVALID_RESPONSE");

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
                "El número de teléfono móvil debe tener exactamente 10 dígitos.");
        register("domain.user.mobile.invalidFormat.user", "El número de celular debe tener 10 dígitos.");
        register("domain.user.notFound.technical", "El usuario solicitado no existe en el sistema.");
        register("domain.user.notFound.user", "No encontramos un usuario con la información suministrada.");
        register("domain.user.email.alreadyRegistered.user", "El correo ya se encuentra registrado.");
        register("domain.user.email.alreadyRegistered.technical", "EMAIL_ALREADY_REGISTERED");
        register("domain.user.idNumber.alreadyRegistered.user", "El número de documento ya se encuentra registrado.");
        register("domain.user.idNumber.alreadyRegistered.technical", "ID_NUMBER_ALREADY_REGISTERED");
        register("domain.user.mobile.alreadyRegistered.user", "El número de móvil ya se encuentra registrado.");
        register("domain.user.mobile.alreadyRegistered.technical", "MOBILE_ALREADY_REGISTERED");
        register("request.payload.invalid", "El cuerpo de la solicitud tiene datos con formato inválido.");
        register("request.payload.invalid.fields",
                "Los campos {fields} deben tener un formato válido (UUID si aplica).");
        register("request.payload.invalid.technical", "INVALID_REQUEST_PAYLOAD");
        register("domain.user.idType.notFound.technical", "El tipo de identificación solicitado no existe.");
        register("domain.user.idType.notFound.user", "El tipo de identificación seleccionado no es válido.");
        register("domain.user.homeCity.notFound.technical", "La ciudad de residencia indicada no existe.");
        register("domain.user.homeCity.notFound.user", "La ciudad de residencia seleccionada no es válida.");
        register("domain.user.email.alreadyConfirmed.technical",
                "El correo electrónico ya fue confirmado anteriormente.");
        register("domain.user.email.alreadyConfirmed.user",
                "El correo electrónico del usuario ya está confirmado.");
        register("domain.user.mobile.alreadyConfirmed.technical",
                "El número de teléfono móvil ya fue confirmado anteriormente.");
        register("domain.user.mobile.alreadyConfirmed.user",
                "El número de teléfono móvil ya está confirmado.");
    }
}
