package co.edu.uco.messageservice.application.usecase;

import co.edu.uco.messageservice.domain.model.MessageDomainAggregate;
import reactor.core.publisher.Mono;

/**
 * Use case that exposes catalog mutations.
 */
public interface MessageCatalogCommandUseCase {

    Mono<MessageDomainAggregate> upsertMessage(MessageDomainAggregate message);

    Mono<MessageDomainAggregate> removeMessage(String key);
}
