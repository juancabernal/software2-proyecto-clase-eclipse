package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.Void;
import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.RequestEmailConfirmationInteractor;
import co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.RequestEmailConfirmationUseCase;

@Service
public class RequestEmailConfirmationInteractorImpl implements RequestEmailConfirmationInteractor {

    private final RequestEmailConfirmationUseCase useCase;

    public RequestEmailConfirmationInteractorImpl(final RequestEmailConfirmationUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public Void execute(final UUID userId) {
        useCase.execute(userId);
        return Void.returnVoid();
    }
}
