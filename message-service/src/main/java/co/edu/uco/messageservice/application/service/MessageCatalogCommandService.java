package co.edu.uco.messageservice.application.service;

import org.springframework.stereotype.Service;

import co.edu.uco.messageservice.application.usecase.MessageCatalogCommandUseCase;
import co.edu.uco.messageservice.domain.model.MessageDomainAggregate;
import co.edu.uco.messageservice.domain.port.MessageCatalogGateway;
import reactor.core.publisher.Mono;

/**
 * Application service that orchestrates write operations over the catalog.
 */
@Service
public class MessageCatalogCommandService implements MessageCatalogCommandUseCase {

    private final MessageCatalogGateway gateway;

    public MessageCatalogCommandService(MessageCatalogGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public Mono<MessageDomainAggregate> upsertMessage(MessageDomainAggregate message) {
        return Mono.from(gateway.save(message));
    }

    @Override
    public Mono<MessageDomainAggregate> removeMessage(String key) {
        return Mono.from(gateway.deleteByKey(key));
    }
}
