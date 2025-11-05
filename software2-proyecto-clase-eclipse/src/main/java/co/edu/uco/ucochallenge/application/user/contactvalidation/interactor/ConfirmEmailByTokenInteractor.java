package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor;

import java.util.UUID;

public interface ConfirmEmailByTokenInteractor {

    void execute(UUID userId, String token);
}
