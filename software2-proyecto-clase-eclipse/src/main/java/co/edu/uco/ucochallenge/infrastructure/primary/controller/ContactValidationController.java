package co.edu.uco.ucochallenge.infrastructure.primary.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.RequestEmailConfirmationInteractor;
import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.RequestMobileConfirmationInteractor;

@RestController
@RequestMapping("/api/v1/users")
public class ContactValidationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContactValidationController.class);

    private final RequestEmailConfirmationInteractor emailInteractor;
    private final RequestMobileConfirmationInteractor mobileInteractor;

    public ContactValidationController(
            final RequestEmailConfirmationInteractor emailInteractor,
            final RequestMobileConfirmationInteractor mobileInteractor) {
        this.emailInteractor = emailInteractor;
        this.mobileInteractor = mobileInteractor;
    }

    @PostMapping("/{userId}/confirm-email")
    public ResponseEntity<String> requestEmailConfirmation(@PathVariable UUID userId) {
        LOGGER.info("📧 Solicitud de confirmación de correo para el usuario {}", userId);
        emailInteractor.execute(userId);
        return ResponseEntity.ok("Correo de confirmación enviado exitosamente.");
    }

    @PostMapping("/{userId}/confirm-mobile")
    public ResponseEntity<String> requestMobileConfirmation(@PathVariable UUID userId) {
        LOGGER.info("📱 Solicitud de confirmación de número móvil para el usuario {}", userId);
        mobileInteractor.execute(userId);
        return ResponseEntity.ok("SMS de confirmación enviado exitosamente.");
    }
}
