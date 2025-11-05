package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor;

import java.util.UUID;

import co.edu.uco.ucochallenge.application.notification.VerificationAttemptResponseDTO;

public interface ValidateEmailLatestConfirmationInteractor {

    VerificationAttemptResponseDTO execute(UUID userId, String code);
}
