package co.edu.uco.api_gateway.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import co.edu.uco.api_gateway.dto.ApiSuccessResponse;
import co.edu.uco.api_gateway.dto.CatalogItemDto;
import co.edu.uco.api_gateway.dto.GetUserResponse;
import co.edu.uco.api_gateway.dto.PageResponse;
import co.edu.uco.api_gateway.dto.RegisterUserResponse;
import co.edu.uco.api_gateway.dto.UserCreateRequest;
import co.edu.uco.api_gateway.dto.UserDto;
import co.edu.uco.api_gateway.dto.VerificationCodeRequest;
import co.edu.uco.api_gateway.services.CatalogServiceProxy;
import co.edu.uco.api_gateway.services.UserServiceProxy;

/**
 * Controlador que expone los endpoints administrativos bajo el prefijo {@code /api/admin} y actúa
 * como fachada frente al microservicio <em>uco-challenge</em> registrado en Eureka.
 */
@RestController
@RequestMapping(path = "/api/admin", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminController {

    private final UserServiceProxy userServiceProxy;
    private final CatalogServiceProxy catalogServiceProxy;

    public AdminController(final UserServiceProxy userServiceProxy, final CatalogServiceProxy catalogServiceProxy) {
        this.userServiceProxy = userServiceProxy;
        this.catalogServiceProxy = catalogServiceProxy;
    }

    /**
     * Proxy de {@code POST /uco-challenge/api/v1/users}.
     *
     * <p>Ejemplo de consumo desde el frontend:
     * <pre>{@code
     * POST /api/admin/users
     * Authorization: Bearer <token>
     * Content-Type: application/json
     * {
     *   "firstName": "Juan",
     *   "firstSurname": "Pérez",
     *   "email": "juan@example.com"
     * }
     * }</pre>
     */
    @PostMapping("/users")
    @PreAuthorize("hasAuthority('administrador')")
    public ResponseEntity<ApiSuccessResponse<RegisterUserResponse>> createUser(
            @RequestBody final UserCreateRequest user,
            @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorizationHeader) {
        final ApiSuccessResponse<RegisterUserResponse> createdUser =
                userServiceProxy.createUser(user, authorizationHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Proxy de {@code GET /uco-challenge/api/v1/users} con soporte de paginación.
     *
     * <p>Ejemplo:
     * <pre>{@code
     * GET /api/admin/users?page=0&size=20
     * Authorization: Bearer <token>
     * }</pre>
     */
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('administrador')")
    public ResponseEntity<ApiSuccessResponse<PageResponse<UserDto>>> getUsers(
            @RequestParam final Map<String, String> queryParams,
            @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorizationHeader) {
        final ApiSuccessResponse<PageResponse<UserDto>> users =
                userServiceProxy.getAllUsers(queryParams, authorizationHeader);
        return ResponseEntity.ok(users);
    }

    /**
     * Proxy de {@code GET /uco-challenge/api/v1/users/{id}}.
     */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasAuthority('administrador')")
    public ResponseEntity<ApiSuccessResponse<GetUserResponse>> getUserById(
            @PathVariable("id") final UUID id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorizationHeader) {
        final ApiSuccessResponse<GetUserResponse> response =
                userServiceProxy.getUserById(id, authorizationHeader);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('administrador')")
    public ResponseEntity<ApiSuccessResponse<Void>> deleteUser(
            @PathVariable("id") final UUID id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorizationHeader) {
        final ApiSuccessResponse<Void> response = userServiceProxy.deleteUser(id, authorizationHeader);
        return ResponseEntity.ok(response);
    }

    /**
    /**
     * Proxy de {@code POST /uco-challenge/api/v1/users/{id}/confirmations/email}.
     */
    @PostMapping("/users/{id}/confirmations/email")
    @PreAuthorize("hasAuthority('administrador')")
    public ResponseEntity<ApiSuccessResponse<Void>> requestEmailConfirmation(
            @PathVariable("id") final UUID id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorizationHeader) {
        final ApiSuccessResponse<Void> response =
                userServiceProxy.requestEmailConfirmation(id, authorizationHeader);
        return ResponseEntity.ok(response);
    }

    /**
     * Proxy de {@code GET /uco-challenge/api/v1/users/{id}/confirmations/email/verify}.
     */
    @GetMapping("/users/{id}/confirmations/email/verify")
    public ResponseEntity<ApiSuccessResponse<Void>> verifyEmail(
            @PathVariable("id") final UUID id,
            @RequestParam(name = "token", required = false) final String token,
            @RequestParam(name = "code", required = false) final String code) {
        final String resolvedToken = resolveToken(token, code);
        if (!StringUtils.hasText(resolvedToken)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El token de verificación es obligatorio.");
        }

        final ApiSuccessResponse<Void> response = userServiceProxy.verifyEmail(id, resolvedToken);
        return ResponseEntity.ok(response);
    }

    /**
     * Proxy de {@code POST /uco-challenge/api/v1/users/{id}/confirmations/email/verify} para
     * verificación manual desde el frontend administrativo.
     */
    @PostMapping("/users/{id}/confirmations/email/verify")
    @PreAuthorize("hasAuthority('administrador')")
    public ResponseEntity<ApiSuccessResponse<Void>> verifyEmailManually(
            @PathVariable("id") final UUID id,
            @RequestBody final VerificationCodeRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorizationHeader) {
        final String resolvedToken = resolveToken(request.token(), request.code());
        if (!StringUtils.hasText(resolvedToken)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El token de verificación es obligatorio.");
        }

        final VerificationCodeRequest sanitized = new VerificationCodeRequest(
                resolvedToken.trim(),
                request.code(),
                request.verifiedAt());

        final ApiSuccessResponse<Void> response =
                userServiceProxy.verifyEmail(id, sanitized, authorizationHeader);
        return ResponseEntity.ok(response);
    }

    /**
     * Proxy de {@code POST /uco-challenge/api/v1/users/{id}/confirmations/mobile}.
     */
    @PostMapping("/users/{id}/confirmations/mobile")
    @PreAuthorize("hasAuthority('administrador')")
    public ResponseEntity<ApiSuccessResponse<Void>> requestMobileConfirmation(
            @PathVariable("id") final UUID id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorizationHeader) {
        final ApiSuccessResponse<Void> response =
                userServiceProxy.requestMobileConfirmation(id, authorizationHeader);
        return ResponseEntity.ok(response);
    }

    /**
     * Proxy de {@code GET /uco-challenge/api/v1/users/{id}/confirmations/mobile/verify}.
     */
    @GetMapping("/users/{id}/confirmations/mobile/verify")
    public ResponseEntity<ApiSuccessResponse<Void>> verifyMobile(
            @PathVariable("id") final UUID id,
            @RequestParam(name = "token", required = false) final String token,
            @RequestParam(name = "code", required = false) final String code) {
        final String resolvedToken = resolveToken(token, code);
        if (!StringUtils.hasText(resolvedToken)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El token de verificación es obligatorio.");
        }

        final ApiSuccessResponse<Void> response = userServiceProxy.verifyMobile(id, resolvedToken);
        return ResponseEntity.ok(response);
    }

    /**
     * Proxy de {@code GET /uco-challenge/api/v1/catalogs/id-types}.
     */
    @GetMapping("/catalogs/id-types")
    @PreAuthorize("hasAuthority('administrador')")
    public ResponseEntity<ApiSuccessResponse<List<CatalogItemDto>>> listIdTypes(
            @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorizationHeader) {
        return ResponseEntity.ok(catalogServiceProxy.listIdTypes(authorizationHeader));
    }
    

    /**
     * Proxy de {@code GET /uco-challenge/api/v1/catalogs/cities}.
     */
    @GetMapping("/catalogs/cities")
    @PreAuthorize("hasAuthority('administrador')")
    public ResponseEntity<ApiSuccessResponse<List<CatalogItemDto>>> listCities(
            @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorizationHeader) {
        return ResponseEntity.ok(catalogServiceProxy.listCities(authorizationHeader));
    }

    /**
     * Proxy de {@code GET /uco-challenge/api/v1/catalogs/departments}.
     */
    @GetMapping("/catalogs/departments")
    @PreAuthorize("hasAuthority('administrador')")
    public ResponseEntity<ApiSuccessResponse<List<CatalogItemDto>>> listDepartments(
            @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorizationHeader) {
        return ResponseEntity.ok(catalogServiceProxy.listDepartments(authorizationHeader));
    }

    /**
     * Proxy de {@code GET /uco-challenge/api/v1/catalogs/departments/{departmentId}/cities}.
     */
    @GetMapping("/catalogs/departments/{departmentId}/cities")
    @PreAuthorize("hasAuthority('administrador')")
    public ResponseEntity<ApiSuccessResponse<List<CatalogItemDto>>> listCitiesByDepartment(
            @PathVariable final String departmentId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorizationHeader) {
        return ResponseEntity.ok(catalogServiceProxy.listCitiesByDepartment(authorizationHeader, departmentId));
    }

    private String resolveToken(final String primary, final String secondary) {
        if (StringUtils.hasText(primary)) {
            return primary.trim();
        }
        return StringUtils.hasText(secondary) ? secondary.trim() : null;
    }

}
