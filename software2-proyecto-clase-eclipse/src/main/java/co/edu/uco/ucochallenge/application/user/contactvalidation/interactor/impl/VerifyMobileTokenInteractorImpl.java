package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.impl;

import java.time.LocalDateTime;
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
    public Void execute(final UUID userId, final UUID tokenId, final String code, final LocalDateTime verificationDate) {
        useCase.execute(userId, tokenId, code, verificationDate);
        return Void.returnVoid();
    }
}
