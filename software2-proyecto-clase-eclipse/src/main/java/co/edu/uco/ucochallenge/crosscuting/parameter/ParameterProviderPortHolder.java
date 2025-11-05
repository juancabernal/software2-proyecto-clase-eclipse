package co.edu.uco.ucochallenge.crosscuting.parameter;

import java.util.concurrent.atomic.AtomicReference;

import co.edu.uco.ucochallenge.crosscuting.helper.ObjectHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.domain.shared.parameter.port.out.ParameterProviderPort;

public final class ParameterProviderPortHolder {

    private static final AtomicReference<ParameterProviderPort> HOLDER =
            new AtomicReference<>(key -> TextHelper.getDefault(key));

    private ParameterProviderPortHolder() {
    }

    public static void configure(final ParameterProviderPort port) {
        HOLDER.set(ObjectHelper.getDefault(port, key -> TextHelper.getDefault(key)));
    }

    public static ParameterProviderPort getService() {
        return HOLDER.get();
    }
}
