package co.edu.uco.ucochallenge.crosscuting.parameter;

import java.util.concurrent.atomic.AtomicReference;

import co.edu.uco.ucochallenge.crosscuting.helper.ObjectHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.infrastructure.secondary.ports.service.ParameterServicePort;

public final class ParameterServicePortHolder {

    private static final AtomicReference<ParameterServicePort> HOLDER =
            new AtomicReference<>(key -> TextHelper.getDefault(key));

    private ParameterServicePortHolder() {
    }

    public static void configure(final ParameterServicePort port) {
        HOLDER.set(ObjectHelper.getDefault(port, key -> TextHelper.getDefault(key)));
    }

    public static ParameterServicePort getService() {
        return HOLDER.get();
    }
}
