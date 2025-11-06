package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor;

import java.util.UUID;

import co.edu.uco.ucochallenge.application.notification.ConfirmationResponseDTO;
import co.edu.uco.ucochallenge.application.notification.VerificationChannel;

public interface SendVerificationCodeInteractor {

    ConfirmationResponseDTO execute(UUID userId, VerificationChannel channel);
}
