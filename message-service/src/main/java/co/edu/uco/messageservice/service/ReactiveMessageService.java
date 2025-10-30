package co.edu.uco.messageservice.service;

import org.springframework.stereotype.Service;

import co.edu.uco.messageservice.catalog.Message;
import co.edu.uco.messageservice.catalog.MessageChange;
import co.edu.uco.messageservice.catalog.ReactiveMessageCatalog;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio de alto nivel que orquesta las operaciones sobre el catálogo
 * reactivo.
 */
@Service
public class ReactiveMessageService {

    private final ReactiveMessageCatalog catalog;

    public ReactiveMessageService(ReactiveMessageCatalog catalog) {
        this.catalog = catalog;
    }

    public Flux<Message> findAll() {
        return catalog.findAll();
    }

    public Mono<Message> findByKey(String key) {
        return catalog.findByKey(key);
    }

    public Mono<Message> upsert(Message message) {
        return catalog.save(message);
    }

    public Mono<Message> delete(String key) {
        return catalog.remove(key);
    }

    public Flux<MessageChange> listenChanges() {
        return catalog.changes();
    }
}
