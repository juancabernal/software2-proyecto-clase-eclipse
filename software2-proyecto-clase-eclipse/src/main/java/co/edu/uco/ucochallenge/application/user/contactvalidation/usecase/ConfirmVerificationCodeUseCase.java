package co.edu.uco.ucochallenge.application.user.contactvalidation.usecase;

import java.util.UUID;

import co.edu.uco.ucochallenge.application.notification.VerificationChannel;
import co.edu.uco.ucochallenge.application.notification.VerificationAttemptResponseDTO;

public interface ConfirmVerificationCodeUseCase {

    VerificationAttemptResponseDTO execute(UUID userId, VerificationChannel channel, String code);
}
