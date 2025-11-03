package co.edu.uco.ucochallenge.shared;

import java.util.Map;
import java.util.Objects;

import co.edu.uco.ucochallenge.infrastructure.secondary.ports.service.MessageServicePort;

public final class InMemoryMessageServicePort implements MessageServicePort {

    private final Map<String, String> messages;

    public InMemoryMessageServicePort(final Map<String, String> messages) {
        this.messages = Map.copyOf(Objects.requireNonNull(messages));
    }

    @Override
    public String getMessage(final String key) {
        return resolveMessage(key, Map.of());
    }

    @Override
    public String getMessage(final String key, final Map<String, String> parameters) {
        return resolveMessage(key, parameters);
    }

    private String resolveMessage(final String key, final Map<String, String> parameters) {
        final String template = messages.getOrDefault(key, key);
        if (parameters == null || parameters.isEmpty()) {
            return template;
        }

        String resolved = template;
        for (final Map.Entry<String, String> entry : parameters.entrySet()) {
            resolved = resolved.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return resolved;
    }
}
