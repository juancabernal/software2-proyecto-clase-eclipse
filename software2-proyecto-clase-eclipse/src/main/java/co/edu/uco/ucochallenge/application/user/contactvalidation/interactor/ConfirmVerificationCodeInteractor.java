package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor;

import java.util.UUID;

import co.edu.uco.ucochallenge.application.notification.VerificationAttemptResponseDTO;
import co.edu.uco.ucochallenge.application.notification.VerificationChannel;

public interface ConfirmVerificationCodeInteractor {

    VerificationAttemptResponseDTO execute(UUID userId, VerificationChannel channel, String code);
}
