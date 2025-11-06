package co.edu.uco.messageservice.infrastructure.primary.rest;

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

import co.edu.uco.messageservice.application.usecase.MessageCatalogChangeUseCase;
import co.edu.uco.messageservice.application.usecase.MessageCatalogCommandUseCase;
import co.edu.uco.messageservice.application.usecase.MessageCatalogQueryUseCase;
import co.edu.uco.messageservice.domain.event.MessageCatalogEventType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Primary REST adapter that exposes the catalog operations through HTTP.
 */
@RestController
@RequestMapping("/api/v1/messages")
public class MessagePrimaryRestController {

    private static final CacheControl NO_CACHE = CacheControl.noStore().mustRevalidate();

    private final MessageCatalogQueryUseCase queryUseCase;
    private final MessageCatalogCommandUseCase commandUseCase;
    private final MessageCatalogChangeUseCase changeUseCase;
    private final MessageRestMapper mapper;

    public MessagePrimaryRestController(MessageCatalogQueryUseCase queryUseCase,
            MessageCatalogCommandUseCase commandUseCase,
            MessageCatalogChangeUseCase changeUseCase,
            MessageRestMapper mapper) {
        this.queryUseCase = queryUseCase;
        this.commandUseCase = commandUseCase;
        this.changeUseCase = changeUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public Flux<MessageRestResource> getAll() {
        return queryUseCase.retrieveAllMessages()
                .map(mapper::toResource);
    }

    @GetMapping("/{key}")
    public Mono<ResponseEntity<MessageRestResource>> getMessage(@PathVariable String key) {
        return queryUseCase.retrieveMessage(key)
                .map(mapper::toResource)
                .map(this::okResponse)
                .switchIfEmpty(Mono.just(notFoundResponse()));
    }

    @PostMapping
    public Mono<ResponseEntity<MessageRestResource>> createMessage(@RequestBody MessageRestResource body) {
        return commandUseCase.upsertMessage(mapper.toDomain(body))
                .map(mapper::toResource)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED)
                        .cacheControl(NO_CACHE)
                        .header("Pragma", "no-cache")
                        .header("Expires", "0")
                        .body(saved));
    }

    @PutMapping("/{key}")
    public Mono<ResponseEntity<MessageRestResource>> modifyMessage(@PathVariable String key,
            @RequestBody MessageRestResource body) {
        MessageRestResource sanitized = new MessageRestResource(key, body.getValue());
        return commandUseCase.upsertMessage(mapper.toDomain(sanitized))
                .map(mapper::toResource)
                .map(this::okResponse);
    }

    @DeleteMapping("/{key}")
    public Mono<ResponseEntity<Void>> deleteMessage(@PathVariable String key) {
        return commandUseCase.removeMessage(key)
                .map(removed -> ResponseEntity.noContent()
                        .cacheControl(NO_CACHE)
                        .header("Pragma", "no-cache")
                        .header("Expires", "0")
                        .build())
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/stream", produces = "text/event-stream")
    public Flux<ServerSentEvent<MessageRestResource>> streamUpdates() {
        return changeUseCase.observeCatalogChanges()
                .filter(change -> change.type() != MessageCatalogEventType.DELETED)
                .map(change -> ServerSentEvent.<MessageRestResource>builder(mapper.toResource(change.message()))
                        .event(change.type().name())
                        .build());
    }

    private ResponseEntity<MessageRestResource> okResponse(MessageRestResource resource) {
        return ResponseEntity.ok()
                .cacheControl(NO_CACHE)
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(resource);
    }

    private ResponseEntity<MessageRestResource> notFoundResponse() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .cacheControl(NO_CACHE)
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .build();
    }
}
