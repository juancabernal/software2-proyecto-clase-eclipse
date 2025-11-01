package co.edu.uco.messageservice.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uco.messageservice.catalog.CatalogEventType;
import co.edu.uco.messageservice.catalog.Message;
import co.edu.uco.messageservice.service.ReactiveMessageService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {

    private static final CacheControl NO_CACHE = CacheControl.noStore().mustRevalidate();

    private final ReactiveMessageService service;

    public MessageController(ReactiveMessageService service) {
        this.service = service;
    }

    @GetMapping
    public Flux<Message> getAll() {
        return service.findAll();
    }

    @GetMapping("/{key}")
    public Mono<ResponseEntity<Message>> getMessage(@PathVariable String key) {
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
    public Mono<ResponseEntity<Message>> createMessage(@RequestBody Message body) {
        Message sanitized = new Message(body.getKey(), body.getValue());
        return service.upsert(sanitized)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED)
                        .cacheControl(NO_CACHE)
                        .header("Pragma", "no-cache")
                        .header("Expires", "0")
                        .body(saved));
    }

    @PutMapping("/{key}")
    public Mono<ResponseEntity<Message>> modifyMessage(@PathVariable String key, @RequestBody Message body) {
        Message sanitized = new Message(key, body.getValue());
        return service.upsert(sanitized)
                .map(saved -> ResponseEntity.ok()
                        .cacheControl(NO_CACHE)
                        .header("Pragma", "no-cache")
                        .header("Expires", "0")
                        .body(saved));
    }

	/*
	 * @DeleteMapping("/{key}") public Mono<ResponseEntity<Void>>
	 * deleteMessage(@PathVariable String key) { return service.delete(key)
	 * .map(removed -> ResponseEntity.noContent() .cacheControl(NO_CACHE)
	 * .header("Pragma", "no-cache") .header("Expires", "0") .build())
	 * .defaultIfEmpty(ResponseEntity.notFound().build()); }
	 * 
	 * @GetMapping(value = "/stream", produces = "text/event-stream") public
	 * Flux<ServerSentEvent<Message>> streamUpdates() { return
	 * service.listenChanges() .filter(change -> change.type() !=
	 * CatalogEventType.DELETED) .map(change ->
	 * ServerSentEvent.<Message>builder(change.payload())
	 * .event(change.type().name()) .build()); }
	 */
}
