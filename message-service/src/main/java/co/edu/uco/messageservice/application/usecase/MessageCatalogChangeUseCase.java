package co.edu.uco.messageservice.application.usecase;

import co.edu.uco.messageservice.domain.event.MessageCatalogChangeEvent;
import reactor.core.publisher.Flux;

/**
 * Use case that allows the application to listen to catalog change events.
 */
public interface MessageCatalogChangeUseCase {

    Flux<MessageCatalogChangeEvent> observeCatalogChanges();
}
