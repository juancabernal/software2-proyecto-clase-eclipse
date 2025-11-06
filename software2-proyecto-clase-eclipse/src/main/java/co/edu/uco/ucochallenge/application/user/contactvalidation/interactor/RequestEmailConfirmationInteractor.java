package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor;

import java.util.UUID;

import co.edu.uco.ucochallenge.application.user.contactvalidation.dto.EmailConfirmationResponseDTO;
import co.edu.uco.ucochallenge.application.interactor.Interactor;

public interface RequestEmailConfirmationInteractor extends Interactor<UUID, EmailConfirmationResponseDTO> {
}