package co.edu.uco.ucochallenge.application.user.listUsers.usecase.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.Void;
import co.edu.uco.ucochallenge.application.user.listUsers.usecase.ListUsersUseCase;
import co.edu.uco.ucochallenge.domain.user.model.User;
import co.edu.uco.ucochallenge.domain.user.port.out.UserRepository;

@Service
public class ListUsersUseCaseImpl implements ListUsersUseCase {

        private final UserRepository repository;

        public ListUsersUseCaseImpl(final UserRepository repository) {
                this.repository = repository;
        }

        @Override
        public List<User> execute(final Void domain) {
                return repository.findAll();
        }
}
