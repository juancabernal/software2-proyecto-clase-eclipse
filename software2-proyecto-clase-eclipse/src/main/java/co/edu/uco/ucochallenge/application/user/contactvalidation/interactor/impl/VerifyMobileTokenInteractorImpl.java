package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.Void;
import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.VerifyMobileTokenInteractor;
import co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.VerifyMobileTokenUseCase;

@Service
public class VerifyMobileTokenInteractorImpl implements VerifyMobileTokenInteractor {

    private final VerifyMobileTokenUseCase useCase;

    public VerifyMobileTokenInteractorImpl(final VerifyMobileTokenUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public Void execute(final UUID userId, final String token) {
        useCase.execute(userId, token);
        return Void.returnVoid();
    }
}
