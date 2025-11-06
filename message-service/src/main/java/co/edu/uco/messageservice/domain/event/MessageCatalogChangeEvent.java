package co.edu.uco.messageservice.domain.event;

import co.edu.uco.messageservice.domain.model.MessageDomainAggregate;

/**
 * Domain event emitted whenever a catalog entry is created, updated or deleted.
 */
public record MessageCatalogChangeEvent(MessageCatalogEventType type, MessageDomainAggregate message) {
}
