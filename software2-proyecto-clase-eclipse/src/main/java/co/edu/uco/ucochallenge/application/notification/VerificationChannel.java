package co.edu.uco.ucochallenge.application.notification;

import java.util.Locale;

import co.edu.uco.ucochallenge.crosscuting.exception.DomainException;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;

public enum VerificationChannel {
    EMAIL("email"),
    MOBILE("mobile");

    private final String value;

    VerificationChannel(final String value) {
        this.value = value;
    }

    public boolean isEmail() {
        return this == EMAIL;
    }

    public boolean isMobile() {
        return this == MOBILE;
    }

    public String value() {
        return value;
    }

    public static VerificationChannel from(final String rawChannel) {
        final String sanitized = TextHelper.getDefaultWithTrim(rawChannel);
        if (TextHelper.isEmpty(sanitized)) {
            throw DomainException.build("Canal de verificación inválido");
        }
        final String normalized = sanitized.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "email", "correo", "mail" -> EMAIL;
            case "mobile", "sms", "phone", "telefono", "teléfono" -> MOBILE;
            default -> throw DomainException.build("Canal de verificación inválido");
        };
    }
}
