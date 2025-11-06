package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.notification.ConfirmationResponseDTO;
import co.edu.uco.ucochallenge.application.notification.VerificationChannel;
import co.edu.uco.ucochallenge.application.user.contactvalidation.dto.EmailConfirmationResponseDTO;
import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.SendVerificationCodeInteractor;
import co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.RequestEmailConfirmationUseCase;
import co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.RequestMobileConfirmationUseCase;

@Service
public class SendVerificationCodeInteractorImpl implements SendVerificationCodeInteractor {

    private final RequestEmailConfirmationUseCase requestEmailConfirmationUseCase;
    private final RequestMobileConfirmationUseCase requestMobileConfirmationUseCase;

    public SendVerificationCodeInteractorImpl(
            final RequestEmailConfirmationUseCase requestEmailConfirmationUseCase,
            final RequestMobileConfirmationUseCase requestMobileConfirmationUseCase) {
        this.requestEmailConfirmationUseCase = requestEmailConfirmationUseCase;
        this.requestMobileConfirmationUseCase = requestMobileConfirmationUseCase;
    }

    @Override
    public ConfirmationResponseDTO execute(final UUID userId, final VerificationChannel channel) {
        return channel.isEmail()
                ? toConfirmationResponse(requestEmailConfirmationUseCase.execute(userId))
                : requestMobileConfirmationUseCase.execute(userId);
    }

    private ConfirmationResponseDTO toConfirmationResponse(final EmailConfirmationResponseDTO response) {
        return new ConfirmationResponseDTO(
                response.verificationId(),
                response.contact(),
                response.channel(),
                response.remainingSeconds());
    }
}
