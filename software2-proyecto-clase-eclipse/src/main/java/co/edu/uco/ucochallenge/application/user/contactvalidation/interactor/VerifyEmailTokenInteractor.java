package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor;

import java.util.UUID;

import co.edu.uco.ucochallenge.application.Void;

public interface VerifyEmailTokenInteractor {

    Void execute(UUID userId, UUID tokenId, String code);
}
