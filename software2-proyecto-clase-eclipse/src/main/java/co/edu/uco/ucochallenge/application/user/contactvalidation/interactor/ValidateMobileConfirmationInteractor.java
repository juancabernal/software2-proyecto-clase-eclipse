package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor;

import java.util.UUID;

import co.edu.uco.ucochallenge.application.notification.VerificationAttemptResponseDTO;

public interface ValidateMobileConfirmationInteractor {

    VerificationAttemptResponseDTO execute(UUID userId, UUID tokenId, String code);
}
