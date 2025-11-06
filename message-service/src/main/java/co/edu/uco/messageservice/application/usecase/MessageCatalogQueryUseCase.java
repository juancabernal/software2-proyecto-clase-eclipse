package co.edu.uco.messageservice.application.usecase;

import co.edu.uco.messageservice.domain.model.MessageDomainAggregate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Use case that exposes read-only operations for catalog messages.
 */
public interface MessageCatalogQueryUseCase {

    Flux<MessageDomainAggregate> retrieveAllMessages();

    Mono<MessageDomainAggregate> retrieveMessage(String key);
}
