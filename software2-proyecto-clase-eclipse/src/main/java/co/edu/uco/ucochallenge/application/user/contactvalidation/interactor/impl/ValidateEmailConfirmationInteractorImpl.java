package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.impl;

import java.time.LocalDateTime;
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
    public VerificationAttemptResponseDTO execute(final UUID userId,
            final UUID tokenId,
            final String code,
            final LocalDateTime verificationDate) {
        return useCase.execute(userId, tokenId, code, verificationDate);
    }
}
