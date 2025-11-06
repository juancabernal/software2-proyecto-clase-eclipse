package co.edu.uco.ucochallenge.application.user.contactconfirmation.service.dto;

import co.edu.uco.ucochallenge.crosscutting.MessageCodes;
import jakarta.validation.constraints.NotBlank;

public record ConfirmVerificationCodeRequestDTO(
                @NotBlank(message = MessageCodes.VERIFICATION_CHANNEL_REQUIRED) String channel,
                @NotBlank(message = MessageCodes.VERIFICATION_CODE_REQUIRED) String code) {
}
