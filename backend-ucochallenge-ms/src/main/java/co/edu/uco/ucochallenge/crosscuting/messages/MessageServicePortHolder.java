package co.edu.uco.ucochallenge.crosscuting.messages;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import co.edu.uco.ucochallenge.crosscuting.helper.ObjectHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.infrastructure.secondary.ports.service.MessageServicePort;

public final class MessageServicePortHolder {

    private static final AtomicReference<MessageServicePort> HOLDER =
            new AtomicReference<>(new NoOpMessageService());

    private MessageServicePortHolder() {
    }

    public static void configure(final MessageServicePort port) {
        HOLDER.set(ObjectHelper.getDefault(port, new NoOpMessageService()));
    }

    public static MessageServicePort getService() {
        return HOLDER.get();
    }

    private static final class NoOpMessageService implements MessageServicePort {

        @Override
        public String getMessage(final String key) {
            return TextHelper.getDefault(key);
        }

        @Override
        public String getMessage(final String key, final Map<String, String> parameters) {
            return TextHelper.getDefault(key);
        }
    }
}
