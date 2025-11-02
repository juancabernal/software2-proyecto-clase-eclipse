package co.edu.uco.ucochallenge.application.notification;

import java.util.IllegalFormatException;
import java.util.Locale;

import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.infrastructure.secondary.ports.service.ParameterServicePort;

public record NotificationTemplate(String parameterKey, String fallback) {

    public NotificationTemplate {
        parameterKey = TextHelper.getDefaultWithTrim(parameterKey);
        fallback = TextHelper.getDefault(fallback, "");
    }

    public String resolve(final ParameterServicePort parameterServicePort, final Object... arguments) {
        final String template = parameterServicePort != null
                ? TextHelper.getDefault(parameterServicePort.getParameter(parameterKey))
                : TextHelper.getDefault();
        final String format = TextHelper.isEmpty(template) ? fallback : template;
        try {
            return String.format(Locale.getDefault(), format, arguments);
        } catch (final IllegalFormatException exception) {
            return String.format(Locale.getDefault(), fallback, arguments);
        }
    }
}