package co.edu.uco.ucochallenge.application.user.contactvalidation.dto;

import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;

public record VerificationCodeRequestDTO(String code) {

    public String sanitizedCode() {
        return TextHelper.getDefaultWithTrim(code);
    }
}