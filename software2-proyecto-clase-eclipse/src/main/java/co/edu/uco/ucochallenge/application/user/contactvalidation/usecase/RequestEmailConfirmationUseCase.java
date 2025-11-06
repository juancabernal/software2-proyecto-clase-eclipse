package co.edu.uco.ucochallenge.application.user.contactvalidation.usecase;

import java.util.UUID;

import co.edu.uco.ucochallenge.application.user.contactvalidation.dto.EmailConfirmationResponseDTO;

public interface RequestEmailConfirmationUseCase {

    EmailConfirmationResponseDTO execute(UUID userId);
}