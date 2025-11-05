package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.notification.VerificationAttemptResponseDTO;
import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.VerifyEmailTokenInteractor;
import co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.VerifyEmailTokenUseCase;

@Service
public class VerifyEmailTokenInteractorImpl implements VerifyEmailTokenInteractor {

    private final VerifyEmailTokenUseCase useCase;

    public VerifyEmailTokenInteractorImpl(final VerifyEmailTokenUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public VerificationAttemptResponseDTO execute(final UUID userId, final UUID tokenId, final String code) {
        return useCase.execute(userId, tokenId, code);
    }
}
