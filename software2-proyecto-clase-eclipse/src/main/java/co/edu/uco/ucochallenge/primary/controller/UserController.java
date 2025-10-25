package co.edu.uco.ucochallenge.primary.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uco.ucochallenge.crosscuting.messages.MessageKey;
import co.edu.uco.ucochallenge.secondary.ports.service.MessageServicePort;
import co.edu.uco.ucochallenge.user.registeruser.application.interactor.RegisterUserInteractor;
import co.edu.uco.ucochallenge.user.registeruser.application.interactor.dto.RegisterUserInputDTO;

@RestController
@RequestMapping("/uco-challenge/api/v1/users")
public class UserController {
	
        private final RegisterUserInteractor registerUserInteractor;
        private final MessageServicePort messageServicePort;

        public UserController(final RegisterUserInteractor registerUserInteractor,
                        final MessageServicePort messageServicePort) {
                this.registerUserInteractor = registerUserInteractor;
                this.messageServicePort = messageServicePort;
        }

        @PostMapping
        public ResponseEntity<String> registerUser(@RequestBody RegisterUserInputDTO dto) {
                final var message = messageServicePort.getMessage(MessageKey.RegisterUser.SUCCESS);
                var normalizedDTO = RegisterUserInputDTO.normalize(dto.idType(), dto.idNumber(), dto.firstName(), dto.secondName(), dto.firstSurname(), dto.secondSurname(), dto.homeCity(), dto.email(), dto.mobileNumber());

                registerUserInteractor.execute(normalizedDTO);
                return new ResponseEntity<String>(message, HttpStatus.CREATED);
        }

}
