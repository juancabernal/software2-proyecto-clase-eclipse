package co.edu.uco.ucochallenge.application.user.contactvalidation.usecase;

import java.util.UUID;

import co.edu.uco.ucochallenge.application.Void;

public interface RequestMobileConfirmationUseCase {

    Void execute(UUID userId);
}