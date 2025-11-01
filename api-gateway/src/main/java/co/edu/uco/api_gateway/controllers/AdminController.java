package co.edu.uco.api_gateway.controllers;

import co.edu.uco.api_gateway.dto.PageResponse;
import co.edu.uco.api_gateway.dto.UserCreateRequest;
import co.edu.uco.api_gateway.dto.UserDto;
import co.edu.uco.api_gateway.services.UserServiceProxy;
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

import java.util.Map;

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
    public ResponseEntity<PageResponse<UserDto>> getUsers(
            @RequestParam Map<String, String> queryParams,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        PageResponse<UserDto> users = userServiceProxy.getAllUsers(queryParams, authorizationHeader);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('administrador')")
    public ResponseEntity<UserDto> createUser(
            @RequestBody UserCreateRequest user,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        UserDto createdUser = userServiceProxy.createUser(user, authorizationHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
}