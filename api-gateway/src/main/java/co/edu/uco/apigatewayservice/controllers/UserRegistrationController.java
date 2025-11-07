package co.edu.uco.apigatewayservice.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uco.apigatewayservice.dto.RegisterUserRequest;
import co.edu.uco.apigatewayservice.service.UserRegistrationGatewayService;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@Validated
@RestController
@RequestMapping(path = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserRegistrationController {

    private final UserRegistrationGatewayService userRegistrationGatewayService;

    public UserRegistrationController(final UserRegistrationGatewayService userRegistrationGatewayService) {
        this.userRegistrationGatewayService = userRegistrationGatewayService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<?>> registerUser(@Valid @RequestBody final RegisterUserRequest request) {
        return userRegistrationGatewayService.register(request);
    }
}
