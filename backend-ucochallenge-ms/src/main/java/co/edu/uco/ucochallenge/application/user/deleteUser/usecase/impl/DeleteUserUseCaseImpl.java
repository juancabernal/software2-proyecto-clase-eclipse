package co.edu.uco.ucochallenge.application.user.deleteUser.usecase.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.Void;
import co.edu.uco.ucochallenge.application.user.deleteUser.usecase.DeleteUserUseCase;
import co.edu.uco.ucochallenge.domain.user.port.out.UserRepository;

@Service
public class DeleteUserUseCaseImpl implements DeleteUserUseCase {

        private final UserRepository repository;

        public DeleteUserUseCaseImpl(final UserRepository repository) {
                this.repository = repository;
        }

        @Override
        public Void execute(final UUID id) {
                repository.deleteById(id);
                return Void.returnVoid();
        }
}
