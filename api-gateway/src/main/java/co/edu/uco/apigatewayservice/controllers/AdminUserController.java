package co.edu.uco.apigatewayservice.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import co.edu.uco.apigatewayservice.service.UserServiceProxy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/api/admin/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminUserController {

    private final UserServiceProxy userServiceProxy;

    public AdminUserController(UserServiceProxy userServiceProxy) {
        this.userServiceProxy = userServiceProxy;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('administrador')")
    public Mono<ResponseEntity<JsonNode>> registerUser(@RequestBody JsonNode body,
                                                       ServerHttpRequest request) {
        return userServiceProxy.createUser(body, request.getHeaders());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('administrador')")
    public Mono<ResponseEntity<JsonNode>> listUsers(@RequestParam(name = "page", required = false) Integer page,
                                                    @RequestParam(name = "size", required = false) Integer size,
                                                    ServerHttpRequest request) {
        return userServiceProxy.fetchUsers(page, size, request.getHeaders());
    }

    @PostMapping(path = "/{id}/send-code")
    @PreAuthorize("hasAuthority('administrador')")
    public Mono<ResponseEntity<Void>> sendVerificationCode(@PathVariable("id") String id,
                                                           @RequestParam("channel") String channel,
                                                           ServerHttpRequest request) {
        String normalizedChannel = StringUtils.hasText(channel) ? channel.trim() : channel;
        return userServiceProxy.sendVerificationCode(id, normalizedChannel, request.getHeaders());
    }

    @PostMapping(path = "/{id}/confirm-code", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('administrador')")
    public Mono<ResponseEntity<JsonNode>> confirmVerificationCode(@PathVariable("id") String id,
                                                                  @RequestBody JsonNode body,
                                                                  ServerHttpRequest request) {
        return userServiceProxy.confirmVerificationCode(id, body, request.getHeaders());
    }
}
