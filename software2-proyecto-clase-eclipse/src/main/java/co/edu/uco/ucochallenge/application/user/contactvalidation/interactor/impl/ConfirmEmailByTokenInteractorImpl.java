package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.ConfirmEmailByTokenInteractor;
import co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.ConfirmEmailByTokenUseCase;

@Service
public class ConfirmEmailByTokenInteractorImpl implements ConfirmEmailByTokenInteractor {

    private final ConfirmEmailByTokenUseCase useCase;

    public ConfirmEmailByTokenInteractorImpl(final ConfirmEmailByTokenUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public void execute(final UUID userId, final String token) {
        useCase.execute(userId, token);
    }
}
