package co.edu.uco.messageservice.infrastructure.primary.rest;

import org.springframework.stereotype.Component;

import co.edu.uco.messageservice.domain.model.MessageDomainAggregate;

/**
 * Maps REST resources to domain aggregates and vice versa.
 */
@Component
public class MessageRestMapper {

    public MessageDomainAggregate toDomain(MessageRestResource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("The message resource must not be null");
        }
        return MessageDomainAggregate.create(resource.getKey(), resource.getValue());
    }

    public MessageRestResource toResource(MessageDomainAggregate aggregate) {
        if (aggregate == null) {
            throw new IllegalArgumentException("The message aggregate must not be null");
        }
        return new MessageRestResource(aggregate.key(), aggregate.value());
    }
}
