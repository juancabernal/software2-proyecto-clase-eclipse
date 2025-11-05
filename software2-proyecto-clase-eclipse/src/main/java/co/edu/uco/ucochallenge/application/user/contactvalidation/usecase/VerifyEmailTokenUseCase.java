package co.edu.uco.ucochallenge.application.user.contactvalidation.usecase;

import java.time.LocalDateTime;
import java.util.UUID;

public interface VerifyEmailTokenUseCase {

    void execute(UUID userId, UUID tokenId, String code, LocalDateTime verificationDate);
}
