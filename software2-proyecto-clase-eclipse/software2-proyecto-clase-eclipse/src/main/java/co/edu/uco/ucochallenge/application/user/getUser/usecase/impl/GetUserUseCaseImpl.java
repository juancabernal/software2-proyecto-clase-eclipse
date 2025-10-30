package co.edu.uco.ucochallenge.application.user.getUser.usecase.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.user.getUser.usecase.GetUserUseCase;
import co.edu.uco.ucochallenge.crosscuting.exception.DomainException;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageCodes;
import co.edu.uco.ucochallenge.domain.user.model.User;
import co.edu.uco.ucochallenge.domain.user.port.out.UserRepository;

@Service
public class GetUserUseCaseImpl implements GetUserUseCase {

        private final UserRepository repository;

        public GetUserUseCaseImpl(final UserRepository repository) {
                this.repository = repository;
        }

        @Override
        public User execute(final UUID id) {
                return repository.findById(id)
                                .orElseThrow(() -> DomainException.buildFromCatalog(
                                                MessageCodes.Domain.User.NOT_FOUND_TECHNICAL,
                                                MessageCodes.Domain.User.NOT_FOUND_USER));
        }
}
