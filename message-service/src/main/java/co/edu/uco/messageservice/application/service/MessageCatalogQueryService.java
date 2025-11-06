package co.edu.uco.messageservice.application.service;

import org.springframework.stereotype.Service;

import co.edu.uco.messageservice.application.usecase.MessageCatalogQueryUseCase;
import co.edu.uco.messageservice.domain.model.MessageDomainAggregate;
import co.edu.uco.messageservice.domain.port.MessageCatalogGateway;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Application service that resolves read operations over the message catalog.
 */
@Service
public class MessageCatalogQueryService implements MessageCatalogQueryUseCase {

    private final MessageCatalogGateway gateway;

    public MessageCatalogQueryService(MessageCatalogGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public Flux<MessageDomainAggregate> retrieveAllMessages() {
        return Flux.from(gateway.fetchAll());
    }

    @Override
    public Mono<MessageDomainAggregate> retrieveMessage(String key) {
        return Mono.from(gateway.fetchByKey(key));
    }
}
