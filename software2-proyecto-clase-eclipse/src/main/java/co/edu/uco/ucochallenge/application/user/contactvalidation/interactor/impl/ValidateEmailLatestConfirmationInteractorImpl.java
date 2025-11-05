package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.notification.VerificationAttemptResponseDTO;
import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.ValidateEmailLatestConfirmationInteractor;
import co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.ValidateEmailLatestConfirmationUseCase;

@Service
public class ValidateEmailLatestConfirmationInteractorImpl implements ValidateEmailLatestConfirmationInteractor {

    private final ValidateEmailLatestConfirmationUseCase useCase;

    public ValidateEmailLatestConfirmationInteractorImpl(final ValidateEmailLatestConfirmationUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public VerificationAttemptResponseDTO execute(final UUID userId, final String code) {
        return useCase.execute(userId, code);
    }
}
