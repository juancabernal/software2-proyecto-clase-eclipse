

package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.Void;
import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.RequestMobileConfirmationInteractor;
import co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.RequestMobileConfirmationUseCase;

@Service
public class RequestMobileConfirmationInteractorImpl implements RequestMobileConfirmationInteractor {

    private final RequestMobileConfirmationUseCase useCase;

    public RequestMobileConfirmationInteractorImpl(final RequestMobileConfirmationUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public Void execute(final UUID userId) {
        useCase.execute(userId);
        return Void.returnVoid();
    }
}
