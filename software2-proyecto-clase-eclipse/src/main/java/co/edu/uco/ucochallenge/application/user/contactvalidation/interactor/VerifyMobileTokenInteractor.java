package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor;

import java.util.UUID;

import co.edu.uco.ucochallenge.application.Void;

public interface VerifyMobileTokenInteractor {

    Void execute(UUID userId, String token);
}
