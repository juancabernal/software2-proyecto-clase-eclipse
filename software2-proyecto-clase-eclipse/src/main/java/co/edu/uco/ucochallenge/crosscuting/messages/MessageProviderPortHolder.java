package co.edu.uco.ucochallenge.crosscuting.messages;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import co.edu.uco.ucochallenge.crosscuting.helper.ObjectHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.domain.shared.message.port.out.MessageProviderPort;

public final class MessageProviderPortHolder {

    private static final AtomicReference<MessageProviderPort> HOLDER = new AtomicReference<>(new NoOpMessageService());

    private MessageProviderPortHolder() {
    }

    public static void configure(final MessageProviderPort port) {
        HOLDER.set(ObjectHelper.getDefault(port, new NoOpMessageService()));
    }

    public static MessageProviderPort getService() {
        return HOLDER.get();
    }

    private static final class NoOpMessageService implements MessageProviderPort {

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
