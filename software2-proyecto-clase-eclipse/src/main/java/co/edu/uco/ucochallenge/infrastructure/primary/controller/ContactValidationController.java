package co.edu.uco.ucochallenge.infrastructure.primary.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uco.ucochallenge.application.notification.ConfirmationResponseDTO;
import co.edu.uco.ucochallenge.application.notification.VerificationAttemptResponseDTO;
import co.edu.uco.ucochallenge.application.user.contactvalidation.dto.VerificationCodeRequestDTO;
import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.RequestEmailConfirmationInteractor;
import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.RequestMobileConfirmationInteractor;
import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.ValidateEmailConfirmationInteractor;
import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.ValidateMobileConfirmationInteractor;
import co.edu.uco.ucochallenge.infrastructure.primary.controller.response.ApiSuccessResponse;

@RestController
@RequestMapping("/api/v1/users")
public class ContactValidationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContactValidationController.class);

    private final RequestEmailConfirmationInteractor emailInteractor;
    private final RequestMobileConfirmationInteractor mobileInteractor;
    private final ValidateEmailConfirmationInteractor validateEmailInteractor;
    private final ValidateMobileConfirmationInteractor validateMobileInteractor;

    public ContactValidationController(
            final RequestEmailConfirmationInteractor emailInteractor,
            final RequestMobileConfirmationInteractor mobileInteractor,
            final ValidateEmailConfirmationInteractor validateEmailInteractor,
            final ValidateMobileConfirmationInteractor validateMobileInteractor) {
        this.emailInteractor = emailInteractor;
        this.mobileInteractor = mobileInteractor;
        this.validateEmailInteractor = validateEmailInteractor;
        this.validateMobileInteractor = validateMobileInteractor;
    }

    @PostMapping("/{userId}/confirm-email")
    public ResponseEntity<ApiSuccessResponse<ConfirmationResponseDTO>> requestEmailConfirmation(@PathVariable UUID userId) {
        LOGGER.info("📧 Solicitud de confirmación de correo para el usuario {}", userId);
        final ConfirmationResponseDTO response = emailInteractor.execute(userId);
        return ResponseEntity.ok(ApiSuccessResponse.of("Correo de confirmación enviado exitosamente.", response));
    }

    @PostMapping("/{userId}/confirm-mobile")
    public ResponseEntity<ApiSuccessResponse<ConfirmationResponseDTO>> requestMobileConfirmation(@PathVariable UUID userId) {
        LOGGER.info("📱 Solicitud de confirmación de número móvil para el usuario {}", userId);

        final ConfirmationResponseDTO response = mobileInteractor.execute(userId);
        return ResponseEntity.ok(ApiSuccessResponse.of("SMS de confirmación enviado exitosamente.", response));
    }

    @PostMapping("/{userId}/confirm-email/verify")
    public ResponseEntity<ApiSuccessResponse<VerificationAttemptResponseDTO>> validateEmailConfirmation(
            @PathVariable UUID userId,
            @RequestBody VerificationCodeRequestDTO request) {
        final VerificationAttemptResponseDTO response = validateEmailInteractor.execute(userId, request.sanitizedCode());
        return ResponseEntity.ok(ApiSuccessResponse.of(response.message(), response));
    }

    @PostMapping("/{userId}/confirm-mobile/verify")
    public ResponseEntity<ApiSuccessResponse<VerificationAttemptResponseDTO>> validateMobileConfirmation(
            @PathVariable UUID userId,
            @RequestBody VerificationCodeRequestDTO request) {
        final VerificationAttemptResponseDTO response = validateMobileInteractor.execute(userId, request.sanitizedCode());
        return ResponseEntity.ok(ApiSuccessResponse.of(response.message(), response));
    }
}
