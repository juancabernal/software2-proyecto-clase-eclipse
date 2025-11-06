package co.edu.uco.messageservice.domain.model;

import java.util.Objects;

/**
 * Domain aggregate that represents a catalog message. It encapsulates
 * validation rules to ensure that the key and value remain consistent across
 * the application.
 */
public record MessageDomainAggregate(String key, String value) {

    private static final int MIN_KEY_LENGTH = 1;

    public MessageDomainAggregate {
        String sanitizedKey = Objects.requireNonNull(key, "The message key must not be null").trim();
        if (sanitizedKey.length() < MIN_KEY_LENGTH) {
            throw new IllegalArgumentException("The message key must contain at least one non-blank character");
        }
        key = sanitizedKey;
        value = Objects.requireNonNull(value, "The message value must not be null").trim();
    }

    public static MessageDomainAggregate create(String key, String value) {
        return new MessageDomainAggregate(key, value);
    }
}
