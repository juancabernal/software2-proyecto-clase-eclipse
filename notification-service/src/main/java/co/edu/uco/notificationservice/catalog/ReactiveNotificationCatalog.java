package co.edu.uco.notificationservice.catalog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * Catálogo reactivo en memoria que gestiona las plantillas de notificación y
 * emite eventos cuando cambian.
 */
@Component
public class ReactiveNotificationCatalog {

    private final Map<String, Notification> notifications = new ConcurrentHashMap<>();
    private final Sinks.Many<NotificationChange> sink = Sinks.many().replay().latest();
    private final Flux<NotificationChange> changeStream = sink.asFlux();

    public ReactiveNotificationCatalog() {
        loadDefaults();
    }

    public Flux<Notification> findAll() {
        return Flux.defer(() -> Flux.fromIterable(notifications.values()).map(this::copyOf));
    }

    public Mono<Notification> findByKey(String key) {
        return Mono.defer(() -> Mono.justOrEmpty(notifications.get(key)).map(this::copyOf));
    }

    public Mono<Notification> save(Notification notification) {
        return Mono.fromSupplier(() -> {
            Notification sanitized = copyOf(notification);
            CatalogEventType type = notifications.containsKey(sanitized.getKey()) ? CatalogEventType.UPDATED
                    : CatalogEventType.CREATED;
            notifications.put(sanitized.getKey(), sanitized);
            emit(type, sanitized);
            return copyOf(sanitized);
        });
    }

    public Mono<Notification> remove(String key) {
        return Mono.defer(() -> {
            Notification removed = notifications.remove(key);
            if (removed == null) {
                return Mono.empty();
            }
            emit(CatalogEventType.DELETED, removed);
            return Mono.just(copyOf(removed));
        });
    }

    public Flux<NotificationChange> changes() {
        return changeStream;
    }

    private void emit(CatalogEventType type, Notification notification) {
        sink.emitNext(new NotificationChange(type, copyOf(notification)), Sinks.EmitFailureHandler.FAIL_FAST);
    }

    private Notification copyOf(Notification notification) {
        return new Notification(notification.getKey(), notification.getChannel(), notification.getSubject(),
                notification.getBody());
    }

    private void register(String key, String channel, String subject, String body) {
        notifications.put(key, new Notification(key, channel, subject, body));
    }

    private void loadDefaults() {
        register("notification.duplicated.email.owner", "EMAIL", "Intento de registro duplicado",
                "Hola {name}, detectamos un intento de registro con tu correo {email}. Si no fuiste tú, por favor "
                        + "contáctanos para proteger tu cuenta.");
        register("notification.duplicated.email.admin", "EMAIL", "Intento de registro duplicado detectado",
                "Se detectó un registro duplicado con el correo {email}. Revisa el caso y toma las acciones "
                        + "correspondientes.");
        register("notification.duplicated.mobile.owner", "SMS", "",
                "Hola {name}, detectamos un intento de registro con tu número {mobile}. Si no fuiste tú, "
                        + "comunícate con soporte.");
        register("notification.confirmation.email.request", "EMAIL", "Confirma tu correo electrónico",
                "Hola {name}, completa tu registro confirmando tu correo en el siguiente enlace: {confirmationLink}.");
        register("notification.confirmation.mobile.request", "SMS", "",
                "Tu código de confirmación es {code}. Utilízalo en los próximos {expirationMinutes} minutos.");
        register("notification.registration.completed.owner", "EMAIL", "Registro completado",
                "Hola {name}, tu registro en UCO Challenge fue exitoso. ¡Bienvenido!");
        register("notification.registration.completed.admin", "EMAIL", "Nuevo usuario registrado",
                "Se registró el usuario {name} con identificación {idType}-{idNumber}.");
        register("notification.validation.code.retry", "SMS", "",
                "Hemos generado un nuevo código de validación: {code}. Recuerda que expirará en {expirationMinutes} minutos.");
    }
}
