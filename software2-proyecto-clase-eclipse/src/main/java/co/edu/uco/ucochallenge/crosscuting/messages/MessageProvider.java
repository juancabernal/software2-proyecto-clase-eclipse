package co.edu.uco.ucochallenge.crosscuting.messages;

import java.util.Collections;
import java.util.Map;

import co.edu.uco.ucochallenge.crosscuting.helper.ObjectHelper;

public final class MessageProvider {

    private MessageProvider() {
    }

    public static String getMessage(final String key) {
        return MessageProviderPortHolder.getService().getMessage(key);
    }

    public static String getMessage(final String key, final Map<String, String> parameters) {
        final Map<String, String> safeParameters = ObjectHelper.getDefault(parameters, Collections.emptyMap());
        return MessageProviderPortHolder.getService().getMessage(key, safeParameters);
    }
}
