package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.notification.VerificationAttemptResponseDTO;
import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.ValidateEmailConfirmationInteractor;
import co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.ValidateEmailConfirmationUseCase;

@Service
public class ValidateEmailConfirmationInteractorImpl implements ValidateEmailConfirmationInteractor {

    private final ValidateEmailConfirmationUseCase useCase;

    public ValidateEmailConfirmationInteractorImpl(final ValidateEmailConfirmationUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public VerificationAttemptResponseDTO execute(final UUID userId, final String code) {
        return useCase.execute(userId, code);
    }
}
