package co.edu.uco.messageservice.domain.port;

import org.reactivestreams.Publisher;

import co.edu.uco.messageservice.domain.event.MessageCatalogChangeEvent;
import co.edu.uco.messageservice.domain.model.MessageDomainAggregate;

/**
 * Port that exposes the catalog operations required by the domain. The
 * implementation is provided by secondary adapters.
 */
public interface MessageCatalogGateway {

    Publisher<MessageDomainAggregate> fetchAll();

    Publisher<MessageDomainAggregate> fetchByKey(String key);

    Publisher<MessageDomainAggregate> save(MessageDomainAggregate message);

    Publisher<MessageDomainAggregate> deleteByKey(String key);

    Publisher<MessageCatalogChangeEvent> listenChanges();
}
