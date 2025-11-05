package co.edu.uco.ucochallenge.application.user.contactvalidation.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;

import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.UUIDHelper;

public record VerificationCodeRequestDTO(@JsonAlias({"token"}) UUID tokenId, String code) {

    public UUID sanitizedTokenId() {
        return UUIDHelper.getDefault(tokenId);
    }

    public String sanitizedCode() {
        return TextHelper.getDefaultWithTrim(code);
    }
}