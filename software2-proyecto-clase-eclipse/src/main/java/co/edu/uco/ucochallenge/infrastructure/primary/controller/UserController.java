package co.edu.uco.ucochallenge.infrastructure.primary.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uco.ucochallenge.application.Void;
import co.edu.uco.ucochallenge.application.notification.ConfirmationResponseDTO;
import co.edu.uco.ucochallenge.application.notification.VerificationAttemptResponseDTO;
import co.edu.uco.ucochallenge.application.pagination.dto.PaginationRequestDTO;
import co.edu.uco.ucochallenge.application.user.contactvalidation.dto.VerificationCodeRequestDTO;
import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.RequestEmailConfirmationInteractor;
import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.RequestMobileConfirmationInteractor;
import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.ValidateMobileConfirmationInteractor;
import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.VerifyEmailTokenInteractor;
import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.VerifyMobileTokenInteractor;
import co.edu.uco.ucochallenge.application.user.deleteUser.interactor.DeleteUserInteractor;
import co.edu.uco.ucochallenge.application.user.getUser.dto.GetUserOutputDTO;
import co.edu.uco.ucochallenge.application.user.getUser.interactor.GetUserInteractor;
import co.edu.uco.ucochallenge.application.user.listUsers.dto.ListUsersRequestDTO;
import co.edu.uco.ucochallenge.application.user.listUsers.dto.ListUsersResponseDTO;
import co.edu.uco.ucochallenge.application.user.listUsers.interactor.ListUsersInteractor;
import co.edu.uco.ucochallenge.application.user.registerUser.dto.RegisterUserInputDTO;
import co.edu.uco.ucochallenge.application.user.registerUser.dto.RegisterUserOutputDTO;
import co.edu.uco.ucochallenge.application.user.registerUser.interactor.RegisterUserInteractor;
import co.edu.uco.ucochallenge.application.user.searchUsers.dto.SearchUsersQueryDTO;
import co.edu.uco.ucochallenge.application.user.searchUsers.interactor.SearchUsersInteractor;
import co.edu.uco.ucochallenge.infrastructure.primary.controller.response.ApiSuccessResponse;

@RestController
@RequestMapping("/uco-challenge/api/v1/users")
public class UserController {


    private final RegisterUserInteractor registerUserInteractor;
    private final ListUsersInteractor listUsersInteractor;
    private final GetUserInteractor getUserInteractor;
    private final DeleteUserInteractor deleteUserInteractor;
    private final RequestEmailConfirmationInteractor requestEmailConfirmationInteractor;
    private final RequestMobileConfirmationInteractor requestMobileConfirmationInteractor;
    private final ValidateMobileConfirmationInteractor validateMobileConfirmationInteractor;
    private final VerifyEmailTokenInteractor verifyEmailTokenInteractor;
    private final VerifyMobileTokenInteractor verifyMobileTokenInteractor;

    /* private final UpdateUserInteractor updateUserInteractor; */

    public UserController(
            final RegisterUserInteractor registerUserInteractor,
            final ListUsersInteractor listUsersInteractor,
            final GetUserInteractor getUserInteractor,
            final DeleteUserInteractor deleteUserInteractor,
            final RequestEmailConfirmationInteractor requestEmailConfirmationInteractor,
            final RequestMobileConfirmationInteractor requestMobileConfirmationInteractor,
            final ValidateMobileConfirmationInteractor validateMobileConfirmationInteractor,
            final VerifyEmailTokenInteractor verifyEmailTokenInteractor,
            final VerifyMobileTokenInteractor verifyMobileTokenInteractor
            /*
             * , final UpdateUserInteractor updateUserInteractor
             */) {
        this.registerUserInteractor = registerUserInteractor;
        this.listUsersInteractor = listUsersInteractor;
        this.getUserInteractor = getUserInteractor;
        this.deleteUserInteractor = deleteUserInteractor;
        this.requestEmailConfirmationInteractor = requestEmailConfirmationInteractor;
        this.requestMobileConfirmationInteractor = requestMobileConfirmationInteractor;
        this.validateMobileConfirmationInteractor = validateMobileConfirmationInteractor;
        this.verifyEmailTokenInteractor = verifyEmailTokenInteractor;
        this.verifyMobileTokenInteractor = verifyMobileTokenInteractor;
        /* this.updateUserInteractor = updateUserInteractor; */
    }

  
    @PostMapping
    public ResponseEntity<ApiSuccessResponse<RegisterUserOutputDTO>> registerUser(
            @RequestBody final RegisterUserInputDTO dto) {
        final RegisterUserOutputDTO response = registerUserInteractor.execute(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiSuccessResponse.of("Usuario registrado exitosamente.", response));
    }

    @GetMapping
    public ResponseEntity<ApiSuccessResponse<ListUsersResponseDTO>> listUsers(
            @RequestParam(name = "page", required = false) final Integer page,
            @RequestParam(name = "size", required = false) final Integer size) {
        final PaginationRequestDTO pagination = PaginationRequestDTO.normalize(page, size);
        final ListUsersResponseDTO response = listUsersInteractor
                .execute(ListUsersRequestDTO.of(pagination));
        return ResponseEntity.ok(ApiSuccessResponse.of("Usuarios obtenidos exitosamente.", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<GetUserOutputDTO>> getUser(@PathVariable("id") final UUID id) {
        final GetUserOutputDTO response = getUserInteractor.execute(id);
        return ResponseEntity.ok(ApiSuccessResponse.of("Usuario obtenido exitosamente.", response));
    }


    @PostMapping("/{id}/confirmations/email/verify")
    public ResponseEntity<ApiSuccessResponse<Void>> verifyEmailManually(
            @PathVariable("id") final UUID id,
            @RequestBody final Map<String, String> requestBody) {
        final String token = requestBody.get("token");
        verifyEmailTokenInteractor.execute(id, token);
        return ResponseEntity.ok(ApiSuccessResponse.of(
                "Correo verificado correctamente.",
                Void.returnVoid()));
    }

    @PostMapping("/{id}/confirmations/mobile/verify")
    public ResponseEntity<ApiSuccessResponse<VerificationAttemptResponseDTO>> validateMobileConfirmation(
            @PathVariable("id") final UUID id,
            @RequestBody final VerificationCodeRequestDTO request) {
        final VerificationAttemptResponseDTO response = validateMobileConfirmationInteractor
                .execute(id, request.sanitizedCode());
        return ResponseEntity.ok(ApiSuccessResponse.of(response.message(), response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<Void>> deleteUser(@PathVariable("id") final UUID id) {
        deleteUserInteractor.execute(id);
        return ResponseEntity.ok(ApiSuccessResponse.of("Usuario eliminado exitosamente.", Void.returnVoid()));
    }

    @PostMapping("/{id}/confirmations/email")
    public ResponseEntity<ApiSuccessResponse<ConfirmationResponseDTO>> requestEmailConfirmation(
            @PathVariable("id") final UUID id) {
        final ConfirmationResponseDTO response = requestEmailConfirmationInteractor.execute(id);
        return ResponseEntity.ok(ApiSuccessResponse.of(
                "Se envió la solicitud de validación del correo electrónico.",
                response));
    }

    @GetMapping("/{id}/confirmations/email/verify")
    public ResponseEntity<ApiSuccessResponse<Void>> verifyEmailConfirmation(
            @PathVariable("id") final UUID id,
            @RequestParam("token") final String token) {
        verifyEmailTokenInteractor.execute(id, token);
        return ResponseEntity.ok(ApiSuccessResponse.of(
                "Correo verificado correctamente.",
                Void.returnVoid()));
    }

    @PostMapping("/{id}/confirmations/mobile")
    public ResponseEntity<ApiSuccessResponse<ConfirmationResponseDTO>> requestMobileConfirmation(
            @PathVariable("id") final UUID id) {
        final ConfirmationResponseDTO response = requestMobileConfirmationInteractor.execute(id);
        return ResponseEntity.ok(ApiSuccessResponse.of(
                "Se envió la solicitud de validación del teléfono móvil.",
                response));
    }

    @GetMapping("/{id}/confirmations/mobile/verify")
    public ResponseEntity<ApiSuccessResponse<Void>> verifyMobileConfirmation(
            @PathVariable("id") final UUID id,
            @RequestParam("token") final String token) {
        verifyMobileTokenInteractor.execute(id, token);
        return ResponseEntity.ok(ApiSuccessResponse.of(
                "Teléfono móvil verificado correctamente.",
                Void.returnVoid()));
    }


    /*
     * @PutMapping("/{id}") public
     * ResponseEntity<ApiSuccessResponse<UpdateUserOutputDTO>> updateUser(
     *
     * @PathVariable("id") final UUID id,
     *
     * @RequestBody final UpdateUserInputDTO dto) { final UpdateUserOutputDTO
     * response = updateUserInteractor .execute(UpdateUserInteractor.Command.of(id,
     * dto)); return
     * ResponseEntity.ok(ApiSuccessResponse.of("Usuario actualizado exitosamente.",
     * response)); }
     */
}
