package co.edu.uco.ucochallenge.domain.shared.message.port.out;

import java.util.Map;

public interface MessageProviderPort {

    String getMessage(String key);

    String getMessage(String key, Map<String, String> parameters);
}
