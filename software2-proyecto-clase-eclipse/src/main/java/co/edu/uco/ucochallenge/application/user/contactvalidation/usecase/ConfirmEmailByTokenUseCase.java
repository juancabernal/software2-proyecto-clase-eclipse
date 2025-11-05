package co.edu.uco.ucochallenge.application.user.contactvalidation.usecase;

import java.util.UUID;

public interface ConfirmEmailByTokenUseCase {

    void execute(UUID userId, String token);
}
