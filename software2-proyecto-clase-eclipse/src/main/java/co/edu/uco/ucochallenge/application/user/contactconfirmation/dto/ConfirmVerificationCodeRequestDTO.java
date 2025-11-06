package co.edu.uco.ucochallenge.application.user.contactconfirmation.dto;

import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;

public record ConfirmVerificationCodeRequestDTO(String channel, String code) {

    public String sanitizedChannel() {
        return TextHelper.getDefaultWithTrim(channel);
    }

    public String sanitizedCode() {
        return TextHelper.getDefaultWithTrim(code);
    }
}
