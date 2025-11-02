package co.edu.uco.ucochallenge.crosscuting.parameter;

import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import co.edu.uco.ucochallenge.crosscuting.exception.InfrastructureException;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageCodes;

public final class ParameterProvider {

    private ParameterProvider() {
    }

    public static String getString(final String key) {
        return ParameterServicePortHolder.getService().getParameter(key);
    }

    public static int getInteger(final String key) {
        final String value = getString(key);
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException exception) {
            throw InfrastructureException.buildFromCatalog(
                    MessageCodes.Infrastructure.ParameterService.INVALID_RESPONSE_TECHNICAL,
                    MessageCodes.Infrastructure.ParameterService.INVALID_RESPONSE_USER,
                    Collections.emptyMap(), exception);
        }
    }

    public static Pattern getPattern(final String key) {
        final String value = getString(key);
        try {
            return Pattern.compile(value);
        } catch (final PatternSyntaxException exception) {
            throw InfrastructureException.buildFromCatalog(
                    MessageCodes.Infrastructure.ParameterService.INVALID_RESPONSE_TECHNICAL,
                    MessageCodes.Infrastructure.ParameterService.INVALID_RESPONSE_USER,
                    Collections.emptyMap(), exception);
        }
    }
}
