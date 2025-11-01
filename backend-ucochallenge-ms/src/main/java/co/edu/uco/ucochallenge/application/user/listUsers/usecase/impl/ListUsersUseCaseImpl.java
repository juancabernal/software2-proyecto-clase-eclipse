package co.edu.uco.ucochallenge.application.user.listUsers.usecase.impl;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.domain.pagination.PageCriteria;
import co.edu.uco.ucochallenge.domain.pagination.PaginatedResult;
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
        public PaginatedResult<User> execute(final PageCriteria domain) {
                return repository.findAll(domain);
        }
}
