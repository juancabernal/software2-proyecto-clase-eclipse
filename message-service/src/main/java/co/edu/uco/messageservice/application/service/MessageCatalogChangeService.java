package co.edu.uco.messageservice.application.service;

import org.springframework.stereotype.Service;

import co.edu.uco.messageservice.application.usecase.MessageCatalogChangeUseCase;
import co.edu.uco.messageservice.domain.event.MessageCatalogChangeEvent;
import co.edu.uco.messageservice.domain.port.MessageCatalogGateway;
import reactor.core.publisher.Flux;

/**
 * Application service that exposes the stream of catalog changes.
 */
@Service
public class MessageCatalogChangeService implements MessageCatalogChangeUseCase {

    private final MessageCatalogGateway gateway;

    public MessageCatalogChangeService(MessageCatalogGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public Flux<MessageCatalogChangeEvent> observeCatalogChanges() {
        return Flux.from(gateway.listenChanges());
    }
}
