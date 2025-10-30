package co.edu.uco.notificationservice.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uco.notificationservice.catalog.CatalogEventType;
import co.edu.uco.notificationservice.catalog.Notification;
import co.edu.uco.notificationservice.service.ReactiveNotificationService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private static final CacheControl NO_CACHE = CacheControl.noStore().mustRevalidate();

    private final ReactiveNotificationService service;

    public NotificationController(ReactiveNotificationService service) {
        this.service = service;
    }

    @GetMapping
    public Flux<Notification> getAll() {
        return service.findAll();
    }

    @GetMapping("/{key}")
    public Mono<ResponseEntity<Notification>> getNotification(@PathVariable String key) {
        return service.findByKey(key)
                .map(value -> ResponseEntity.ok()
                        .cacheControl(NO_CACHE)
                        .header("Pragma", "no-cache")
                        .header("Expires", "0")
                        .body(value))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .cacheControl(NO_CACHE)
                        .header("Pragma", "no-cache")
                        .header("Expires", "0")
                        .build()));
    }

    @PostMapping
    public Mono<ResponseEntity<Notification>> createNotification(@RequestBody Mono<Notification> body) {
        return body.map(notification -> new Notification(notification.getKey(), notification.getChannel(),
                notification.getSubject(), notification.getBody()))
                .flatMap(service::upsert)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED)
                        .cacheControl(NO_CACHE)
                        .header("Pragma", "no-cache")
                        .header("Expires", "0")
                        .body(saved));
    }

    @PutMapping("/{key}")
    public Mono<ResponseEntity<Notification>> modifyNotification(@PathVariable String key,
            @RequestBody Mono<Notification> body) {
        return body.map(value -> new Notification(key, value.getChannel(), value.getSubject(), value.getBody()))
                .flatMap(service::upsert)
                .map(saved -> ResponseEntity.ok()
                        .cacheControl(NO_CACHE)
                        .header("Pragma", "no-cache")
                        .header("Expires", "0")
                        .body(saved));
    }

    /*
     * @DeleteMapping("/{key}") public Mono<ResponseEntity<Void>>
     * deleteNotification(@PathVariable String key) { return service.delete(key)
     * .map(removed -> ResponseEntity.noContent() .cacheControl(NO_CACHE)
     * .header("Pragma", "no-cache") .header("Expires", "0") .build())
     * .defaultIfEmpty(ResponseEntity.notFound().build()); }
     */

    @GetMapping(value = "/stream", produces = "text/event-stream")
    public Flux<ServerSentEvent<Notification>> streamUpdates() {
        return service.listenChanges()
                .filter(change -> change.type() != CatalogEventType.DELETED)
                .map(change -> ServerSentEvent.<Notification>builder(change.payload())
                        .event(change.type().name())
                        .build());
    }
}
