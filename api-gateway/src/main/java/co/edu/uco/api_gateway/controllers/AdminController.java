package co.edu.uco.api_gateway.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/admin", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminController {

    @GetMapping(value = "/verify")
    public ResponseEntity<HttpStatus> publicEndpoint() {
        return ResponseEntity.status(HttpStatus.OK).body(HttpStatus.OK);
    }

}