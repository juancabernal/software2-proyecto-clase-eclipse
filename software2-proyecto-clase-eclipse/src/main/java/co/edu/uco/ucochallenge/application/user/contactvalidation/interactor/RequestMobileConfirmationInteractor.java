package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor;

import java.util.UUID;

import co.edu.uco.ucochallenge.application.notification.ConfirmationResponseDTO;
import co.edu.uco.ucochallenge.application.interactor.Interactor;

public interface RequestMobileConfirmationInteractor extends Interactor<UUID, ConfirmationResponseDTO> {
}
