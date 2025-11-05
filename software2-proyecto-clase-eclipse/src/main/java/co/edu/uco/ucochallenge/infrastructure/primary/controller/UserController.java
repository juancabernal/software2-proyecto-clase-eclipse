package co.edu.uco.ucochallenge.infrastructure.primary.controller;

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
import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.ValidateEmailConfirmationInteractor;
import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.ValidateMobileConfirmationInteractor;
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
        private final SearchUsersInteractor searchUsersInteractor;
        private final DeleteUserInteractor deleteUserInteractor;
        private final RequestEmailConfirmationInteractor requestEmailConfirmationInteractor;
        private final RequestMobileConfirmationInteractor requestMobileConfirmationInteractor;
        private final ValidateEmailConfirmationInteractor validateEmailConfirmationInteractor;
        private final ValidateMobileConfirmationInteractor validateMobileConfirmationInteractor;
 
		/* private final UpdateUserInteractor updateUserInteractor; */

        public UserController(
                        final RegisterUserInteractor registerUserInteractor,
                        final ListUsersInteractor listUsersInteractor,
                        final GetUserInteractor getUserInteractor,
                        final SearchUsersInteractor searchUsersInteractor,
                        final DeleteUserInteractor deleteUserInteractor,
                        final RequestEmailConfirmationInteractor requestEmailConfirmationInteractor,
                        final RequestMobileConfirmationInteractor requestMobileConfirmationInteractor,
                        final ValidateEmailConfirmationInteractor validateEmailConfirmationInteractor,
                        final ValidateMobileConfirmationInteractor validateMobileConfirmationInteractor
				/*
																 * , final UpdateUserInteractor updateUserInteractor
																 */) {
                this.registerUserInteractor = registerUserInteractor;
                this.listUsersInteractor = listUsersInteractor;
                this.getUserInteractor = getUserInteractor;
                this.searchUsersInteractor = searchUsersInteractor;
                this.deleteUserInteractor = deleteUserInteractor;
                this.requestEmailConfirmationInteractor = requestEmailConfirmationInteractor;
                this.requestMobileConfirmationInteractor = requestMobileConfirmationInteractor;
                this.validateEmailConfirmationInteractor = validateEmailConfirmationInteractor;
                this.validateMobileConfirmationInteractor = validateMobileConfirmationInteractor;
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

    @GetMapping("/search")
    public ResponseEntity<ApiSuccessResponse<ListUsersResponseDTO>> searchUsers(
            @RequestParam(name = "idType", required = false) final UUID idType,
            @RequestParam(name = "idNumber", required = false) final String idNumber,
            @RequestParam(name = "firstName", required = false) final String firstName,
            @RequestParam(name = "firstSurname", required = false) final String firstSurname,
            @RequestParam(name = "homeCity", required = false) final UUID homeCity,
            @RequestParam(name = "email", required = false) final String email,
            @RequestParam(name = "mobileNumber", required = false) final String mobileNumber,
            @RequestParam(name = "q", required = false) final String q,
            @RequestParam(name = "page", required = false) final Integer page,
            @RequestParam(name = "size", required = false) final Integer size) {
        final SearchUsersQueryDTO filter = SearchUsersQueryDTO.normalize(
                idType,
                idNumber,
                firstName,
                firstSurname,
                homeCity,
                email,
                mobileNumber,
                q,
                page,
                size);
        final ListUsersResponseDTO response = searchUsersInteractor.execute(filter);
        return ResponseEntity.ok(ApiSuccessResponse.of("Usuarios filtrados exitosamente.", response));
    }


        
        @PostMapping("/{id}/confirmations/email/verify")
        public ResponseEntity<ApiSuccessResponse<VerificationAttemptResponseDTO>> validateEmailConfirmation(
                        @PathVariable("id") final UUID id,
                        @RequestBody final VerificationCodeRequestDTO request) {
                final VerificationAttemptResponseDTO response = validateEmailConfirmationInteractor
                                .execute(id, request.sanitizedCode());
                return ResponseEntity.ok(ApiSuccessResponse.of(response.message(), response));
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

    @PostMapping("/{id}/confirmations/mobile")
    public ResponseEntity<ApiSuccessResponse<ConfirmationResponseDTO>> requestMobileConfirmation(
            @PathVariable("id") final UUID id) {
        final ConfirmationResponseDTO response = requestMobileConfirmationInteractor.execute(id);
        return ResponseEntity.ok(ApiSuccessResponse.of(
                "Se envió la solicitud de validación del teléfono móvil.",
                response));
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
