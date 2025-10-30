package co.edu.uco.notificationservice.service;

import org.springframework.stereotype.Service;

import co.edu.uco.notificationservice.catalog.Notification;
import co.edu.uco.notificationservice.catalog.NotificationChange;
import co.edu.uco.notificationservice.catalog.ReactiveNotificationCatalog;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio de alto nivel que orquesta las operaciones sobre el catálogo
 * reactivo de notificaciones.
 */
@Service
public class ReactiveNotificationService {

    private final ReactiveNotificationCatalog catalog;

    public ReactiveNotificationService(ReactiveNotificationCatalog catalog) {
        this.catalog = catalog;
    }

    public Flux<Notification> findAll() {
        return catalog.findAll();
    }

    public Mono<Notification> findByKey(String key) {
        return catalog.findByKey(key);
    }

    public Mono<Notification> upsert(Notification notification) {
        return catalog.save(notification);
    }

    public Mono<Notification> delete(String key) {
        return catalog.remove(key);
    }

    public Flux<NotificationChange> listenChanges() {
        return catalog.changes();
    }
}
