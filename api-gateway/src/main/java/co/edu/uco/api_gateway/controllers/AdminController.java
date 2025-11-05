package co.edu.uco.api_gateway.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uco.api_gateway.dto.ApiSuccessResponse;
import co.edu.uco.api_gateway.dto.CatalogItemDto;
import co.edu.uco.api_gateway.dto.PageResponse;
import co.edu.uco.api_gateway.dto.RegisterUserResponse;
import co.edu.uco.api_gateway.dto.UserCreateRequest;
import co.edu.uco.api_gateway.dto.UserDto;
import co.edu.uco.api_gateway.services.UserServiceProxy;
import co.edu.uco.api_gateway.services.CatalogServiceProxy;

@RestController
@RequestMapping(path = "/api/admin", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminController {

    private final UserServiceProxy userServiceProxy;
    private final CatalogServiceProxy catalogServiceProxy;

    public AdminController(UserServiceProxy userServiceProxy, CatalogServiceProxy catalogServiceProxy) {
        this.userServiceProxy = userServiceProxy;
        this.catalogServiceProxy = catalogServiceProxy;
    }

    @GetMapping(value = "/verify")
    @PreAuthorize("hasAuthority('administrador')")
    public ResponseEntity<HttpStatus> verifyAccess() {
        System.out.println("Estoy muestra tin");
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

    @GetMapping("/catalogs/id-types")
    @PreAuthorize("hasAuthority('administrador')")
    public ResponseEntity<ApiSuccessResponse<List<CatalogItemDto>>> listIdTypes(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(catalogServiceProxy.listIdTypes(authorizationHeader));
    }

    @GetMapping("/catalogs/departments")
    @PreAuthorize("hasAuthority('administrador')")
    public ResponseEntity<ApiSuccessResponse<List<CatalogItemDto>>> listDepartments(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(catalogServiceProxy.listDepartments(authorizationHeader));
    }

    @GetMapping("/catalogs/cities")
    @PreAuthorize("hasAuthority('administrador')")
    public ResponseEntity<ApiSuccessResponse<List<CatalogItemDto>>> listCities(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(catalogServiceProxy.listCities(authorizationHeader));
    }

    @GetMapping("/catalogs/departments/{departmentId}/cities")
    @PreAuthorize("hasAuthority('administrador')")
    public ResponseEntity<ApiSuccessResponse<List<CatalogItemDto>>> listCitiesByDepartment(
            @PathVariable String departmentId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity
                .ok(catalogServiceProxy.listCitiesByDepartment(authorizationHeader, departmentId));
    }
}