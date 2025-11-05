package co.edu.uco.ucochallenge.application.user.contactvalidation.usecase;

import java.time.LocalDateTime;
import java.util.UUID;

import co.edu.uco.ucochallenge.application.notification.VerificationAttemptResponseDTO;

public interface ValidateEmailConfirmationUseCase {

    VerificationAttemptResponseDTO execute(UUID userId, UUID tokenId, String code, LocalDateTime verificationDate);
}
