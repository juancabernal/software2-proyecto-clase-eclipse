package co.edu.uco.parametersservice.controller;

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

import co.edu.uco.parametersservice.catalog.CatalogEventType;
import co.edu.uco.parametersservice.catalog.Parameter;
import co.edu.uco.parametersservice.service.ReactiveParameterService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/parameters")
public class ParameterController {

    private static final CacheControl NO_CACHE = CacheControl.noStore().mustRevalidate();
    private static final String VERIFICATION_EXPIRATION_KEY = "validation.code.timeExpiration";
    private static final String VERIFICATION_ATTEMPTS_KEY = "validation.code.maxAttempts";

    private final ReactiveParameterService service;
    

    public ParameterController(ReactiveParameterService service) {
        this.service = service;
    }

    @GetMapping
    public Flux<Parameter> getAll() {
        return service.findAll();
    }

    @GetMapping("/{key}")
    public Mono<ResponseEntity<Parameter>> getParameter(@PathVariable String key) {
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
    public Mono<ResponseEntity<Parameter>> createParameter(@RequestBody Parameter body) {
        Parameter sanitized = new Parameter(body.getKey(), body.getValue());
        return service.upsert(sanitized)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED)
                        .cacheControl(NO_CACHE)
                        .header("Pragma", "no-cache")
                        .header("Expires", "0")
                        .body(saved));
    }

    @PutMapping("/{key}")
    public Mono<ResponseEntity<Parameter>> modifyParameter(@PathVariable String key, @RequestBody Parameter body) {
        Parameter sanitized = new Parameter(key, body.getValue());
        return service.upsert(sanitized)
                .map(saved -> ResponseEntity.ok()
                        .cacheControl(NO_CACHE)
                        .header("Pragma", "no-cache")
                        .header("Expires", "0")
                        .body(saved));
    }
    @PutMapping("/verification/expiration")
    public Mono<ResponseEntity<Parameter>> updateVerificationExpiration(
            @RequestBody VerificationExpirationRequest body) {
        int sanitized = Math.max(body.minutes(), 1);
        Parameter parameter = new Parameter(VERIFICATION_EXPIRATION_KEY, Integer.toString(sanitized));
        return service.upsert(parameter)
                .map(saved -> ResponseEntity.ok()
                        .cacheControl(NO_CACHE)
                        .header("Pragma", "no-cache")
                        .header("Expires", "0")
                        .body(saved));
    }

    @PutMapping("/verification/attempts")
    public Mono<ResponseEntity<Parameter>> updateVerificationAttempts(
            @RequestBody VerificationAttemptsRequest body) {
        int sanitized = Math.max(body.attempts(), 1);
        Parameter parameter = new Parameter(VERIFICATION_ATTEMPTS_KEY, Integer.toString(sanitized));
        return service.upsert(parameter)
                .map(saved -> ResponseEntity.ok()
                        .cacheControl(NO_CACHE)
                        .header("Pragma", "no-cache")
                        .header("Expires", "0")
                        .body(saved));
    }


	/*
	 * @DeleteMapping("/{key}") public Mono<ResponseEntity<Void>>
	 * deleteParameter(@PathVariable String key) { return service.delete(key)
	 * .map(removed -> ResponseEntity.noContent() .cacheControl(NO_CACHE)
	 * .header("Pragma", "no-cache") .header("Expires", "0") .build())
	 * .defaultIfEmpty(ResponseEntity.notFound().build()); }
	 */

    @GetMapping(value = "/stream", produces = "text/event-stream")
    public Flux<ServerSentEvent<Parameter>> streamUpdates() {
        return service.listenChanges()
                .filter(change -> change.type() != CatalogEventType.DELETED)
                .map(change -> ServerSentEvent.<Parameter>builder(change.payload())
                        .event(change.type().name())
                        .build());
    }
    
    public record VerificationExpirationRequest(int minutes) {
    }

    public record VerificationAttemptsRequest(int attempts) {
    }
}
