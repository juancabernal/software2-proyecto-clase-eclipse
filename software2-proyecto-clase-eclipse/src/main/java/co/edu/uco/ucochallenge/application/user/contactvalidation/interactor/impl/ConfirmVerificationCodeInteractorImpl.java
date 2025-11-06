package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.notification.VerificationAttemptResponseDTO;
import co.edu.uco.ucochallenge.application.notification.VerificationChannel;
import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.ConfirmVerificationCodeInteractor;
import co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.ConfirmVerificationCodeUseCase;

@Service
public class ConfirmVerificationCodeInteractorImpl implements ConfirmVerificationCodeInteractor {

    private final ConfirmVerificationCodeUseCase useCase;

    public ConfirmVerificationCodeInteractorImpl(final ConfirmVerificationCodeUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public VerificationAttemptResponseDTO execute(final UUID userId,
            final VerificationChannel channel,
            final String code) {
        return useCase.execute(userId, channel, code);
    }
}
