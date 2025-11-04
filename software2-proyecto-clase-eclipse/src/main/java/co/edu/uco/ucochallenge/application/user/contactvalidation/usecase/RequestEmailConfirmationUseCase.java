package co.edu.uco.ucochallenge.application.user.contactvalidation.usecase;

import java.util.UUID;

import co.edu.uco.ucochallenge.application.notification.ConfirmationResponseDTO;

public interface RequestEmailConfirmationUseCase {

    ConfirmationResponseDTO execute(UUID userId);
}