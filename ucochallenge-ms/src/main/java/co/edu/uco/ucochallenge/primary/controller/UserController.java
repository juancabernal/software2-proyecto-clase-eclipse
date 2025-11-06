package co.edu.uco.ucochallenge.primary.controller;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import co.edu.uco.ucochallenge.user.confirmcontact.application.service.SendVerificationCodeService;
import co.edu.uco.ucochallenge.user.confirmcontact.application.service.UserContactConfirmationService;
import co.edu.uco.ucochallenge.user.confirmcontact.application.service.VerificationChannel;
import co.edu.uco.ucochallenge.user.confirmcontact.application.service.dto.ConfirmVerificationCodeRequestDTO;
import co.edu.uco.ucochallenge.user.confirmcontact.application.service.dto.ConfirmVerificationCodeResponseDTO;
import co.edu.uco.ucochallenge.application.user.registration.dto.UserRegistrationRequestDTO;
import co.edu.uco.ucochallenge.application.user.registration.dto.UserRegistrationResponseDTO;
import co.edu.uco.ucochallenge.application.user.registration.interactor.UserRegistrationApplicationInteractor;
import co.edu.uco.ucochallenge.application.user.search.dto.UserSearchQueryRequestDTO;
import co.edu.uco.ucochallenge.application.user.search.dto.UserSearchQueryResponseDTO;
import co.edu.uco.ucochallenge.application.user.search.interactor.UserSearchApplicationInteractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/uco-challenge/api/v1")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserRegistrationApplicationInteractor userRegistrationInteractor;
    private final UserSearchApplicationInteractor userSearchInteractor;
    private final UserContactConfirmationService userContactConfirmationService;
    private final SendVerificationCodeService sendVerificationCodeService;

    public UserController(final UserRegistrationApplicationInteractor userRegistrationInteractor,
                          final UserSearchApplicationInteractor userSearchInteractor,
                          final UserContactConfirmationService userContactConfirmationService,
                          final SendVerificationCodeService sendVerificationCodeService) {
        this.userRegistrationInteractor = userRegistrationInteractor;
        this.userSearchInteractor = userSearchInteractor;
        this.userContactConfirmationService = userContactConfirmationService;
        this.sendVerificationCodeService = sendVerificationCodeService;
    }

    @PostMapping("/users")
    public ResponseEntity<UserRegistrationResponseDTO> create(
                    @Valid @RequestBody final UserRegistrationRequestDTO request) {
        final UserRegistrationResponseDTO response = userRegistrationInteractor.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/users")
    public ResponseEntity<UserSearchQueryResponseDTO> getUsers(
            @RequestParam(name = "page", required = false) final Integer page,
            @RequestParam(name = "size", required = false) final Integer size) {
        final var normalizedInput = UserSearchQueryRequestDTO.normalize(page, size);
        final var response = userSearchInteractor.execute(normalizedInput);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{id}/send-code")
    public ResponseEntity<Void> sendCode(@PathVariable UUID id,
                                         @RequestParam("channel") String channel,
                                         HttpServletRequest req) {
        log.info("send-code hit: {} {} channel={}", req.getMethod(), req.getRequestURI(), channel);
        final VerificationChannel verificationChannel = VerificationChannel.from(channel);
        sendVerificationCodeService.sendVerificationCode(id, verificationChannel);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/users/{id}/confirm-code")
    public ResponseEntity<ConfirmVerificationCodeResponseDTO> confirmCode(
            @PathVariable UUID id,
            @Valid @RequestBody final ConfirmVerificationCodeRequestDTO request) {
        final VerificationChannel channel = VerificationChannel.from(request.channel());
        userContactConfirmationService.confirmVerificationCode(id, channel, request.code());
        return ResponseEntity.ok(new ConfirmVerificationCodeResponseDTO(true));
    }
}
