package co.edu.uco.ucochallenge.application.user.contactvalidation.usecase;

import java.util.UUID;

public interface VerifyMobileTokenUseCase {

    void execute(UUID userId, String token);
}
