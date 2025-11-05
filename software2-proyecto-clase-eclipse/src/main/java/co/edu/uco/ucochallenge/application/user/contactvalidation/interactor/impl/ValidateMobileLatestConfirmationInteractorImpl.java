package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.notification.VerificationAttemptResponseDTO;
import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.ValidateMobileLatestConfirmationInteractor;
import co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.ValidateMobileLatestConfirmationUseCase;

@Service
public class ValidateMobileLatestConfirmationInteractorImpl implements ValidateMobileLatestConfirmationInteractor {

    private final ValidateMobileLatestConfirmationUseCase useCase;

    public ValidateMobileLatestConfirmationInteractorImpl(final ValidateMobileLatestConfirmationUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public VerificationAttemptResponseDTO execute(final UUID userId, final String code) {
        return useCase.execute(userId, code);
    }
}
