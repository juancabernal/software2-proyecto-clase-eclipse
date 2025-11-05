package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.notification.VerificationAttemptResponseDTO;
import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.ValidateMobileConfirmationInteractor;
import co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.ValidateMobileConfirmationUseCase;

@Service
public class ValidateMobileConfirmationInteractorImpl implements ValidateMobileConfirmationInteractor {

    private final ValidateMobileConfirmationUseCase useCase;

    public ValidateMobileConfirmationInteractorImpl(final ValidateMobileConfirmationUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public VerificationAttemptResponseDTO execute(final UUID userId, final UUID tokenId, final String code) {
        return useCase.execute(userId, tokenId, code);
    }
}
