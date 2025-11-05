package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.ConfirmMobileByTokenInteractor;
import co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.ConfirmMobileByTokenUseCase;

@Service
public class ConfirmMobileByTokenInteractorImpl implements ConfirmMobileByTokenInteractor {

    private final ConfirmMobileByTokenUseCase useCase;

    public ConfirmMobileByTokenInteractorImpl(final ConfirmMobileByTokenUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public void execute(final UUID userId, final String token) {
        useCase.execute(userId, token);
    }
}
