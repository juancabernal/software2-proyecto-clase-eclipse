package co.edu.uco.ucochallenge.application.notification;

import java.util.ArrayList;
import java.util.List;

import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.domain.user.model.User;

public record RegistrationAttempt(
        String firstName,
        String secondName,
        String firstSurname,
        String secondSurname,
        String email,
        String mobileNumber) {

    public RegistrationAttempt {
        firstName = TextHelper.getDefaultWithTrim(firstName);
        secondName = TextHelper.getDefaultWithTrim(secondName);
        firstSurname = TextHelper.getDefaultWithTrim(firstSurname);
        secondSurname = TextHelper.getDefaultWithTrim(secondSurname);
        email = TextHelper.getDefaultWithTrim(email);
        mobileNumber = TextHelper.getDefaultWithTrim(mobileNumber);
    }

    public static RegistrationAttempt fromUser(final User user) {
        if (user == null) {
            return new RegistrationAttempt(null, null, null, null, null, null);
        }
        return new RegistrationAttempt(
                user.firstName(),
                user.secondName(),
                user.firstSurname(),
                user.secondSurname(),
                user.email(),
                user.mobileNumber());
    }

    public String displayName() {
        final List<String> parts = new ArrayList<>();
        appendIfHasText(parts, firstName);
        appendIfHasText(parts, secondName);
        appendIfHasText(parts, firstSurname);
        appendIfHasText(parts, secondSurname);
        return String.join(" ", parts);
    }

    private void appendIfHasText(final List<String> parts, final String value) {
        if (!TextHelper.isEmpty(value)) {
            parts.add(value);
        }
    }
}