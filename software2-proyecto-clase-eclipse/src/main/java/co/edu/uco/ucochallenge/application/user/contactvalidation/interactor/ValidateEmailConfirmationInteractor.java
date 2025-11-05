package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor;

import java.time.LocalDateTime;
import java.util.UUID;

import co.edu.uco.ucochallenge.application.notification.VerificationAttemptResponseDTO;

public interface ValidateEmailConfirmationInteractor {

    VerificationAttemptResponseDTO execute(UUID userId, UUID tokenId, String code, LocalDateTime verificationDate);
}
