package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.Void;
import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.VerifyEmailTokenInteractor;
import co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.VerifyEmailTokenUseCase;

@Service
public class VerifyEmailTokenInteractorImpl implements VerifyEmailTokenInteractor {

    private final VerifyEmailTokenUseCase useCase;

    public VerifyEmailTokenInteractorImpl(final VerifyEmailTokenUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public Void execute(final UUID userId, final UUID tokenId, final String code, final LocalDateTime verificationDate) {
        useCase.execute(userId, tokenId, code, verificationDate);
        return Void.returnVoid();
    }
}
