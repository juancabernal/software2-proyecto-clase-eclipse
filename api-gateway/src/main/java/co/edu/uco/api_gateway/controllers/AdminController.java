package co.edu.uco.api_gateway.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uco.api_gateway.dto.ApiSuccessResponse;
import co.edu.uco.api_gateway.dto.PageResponse;
import co.edu.uco.api_gateway.dto.RegisterUserResponse;
import co.edu.uco.api_gateway.dto.UserCreateRequest;
import co.edu.uco.api_gateway.dto.UserDto;
import co.edu.uco.api_gateway.services.UserServiceProxy;

@RestController
@RequestMapping(path = "/api/admin", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminController {

    private final UserServiceProxy userServiceProxy;

    public AdminController(UserServiceProxy userServiceProxy) {
        this.userServiceProxy = userServiceProxy;
    }

    @GetMapping(value = "/verify")
    @PreAuthorize("hasAuthority('administrador')")
    public ResponseEntity<HttpStatus> verifyAccess() {
        return ResponseEntity.status(HttpStatus.OK).body(HttpStatus.OK);
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('administrador')")
    public ResponseEntity<ApiSuccessResponse<PageResponse<UserDto>>> getUsers(
            @RequestParam Map<String, String> queryParams,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        ApiSuccessResponse<PageResponse<UserDto>> users = userServiceProxy.getAllUsers(queryParams, authorizationHeader);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('administrador')")
    public ResponseEntity<ApiSuccessResponse<RegisterUserResponse>> createUser(
            @RequestBody UserCreateRequest user,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        ApiSuccessResponse<RegisterUserResponse> createdUser = userServiceProxy.createUser(user, authorizationHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/users/departments")
    @PreAuthorize("hasAuthority('administrador')")
    public ResponseEntity<List<Map<String, Object>>> listDepartments(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(userServiceProxy.listDepartments(authorizationHeader));
    }

    @GetMapping("/users/cities")
    @PreAuthorize("hasAuthority('administrador')")
    public ResponseEntity<List<Map<String, Object>>> listCities(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(userServiceProxy.listCities(authorizationHeader));
    }

    @GetMapping("/users/id-types")
    @PreAuthorize("hasAuthority('administrador')")
    public ResponseEntity<List<Map<String, Object>>> listIdTypes(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(userServiceProxy.listIdTypes(authorizationHeader));
    }
}