package co.edu.uco.ucochallenge.application.user.searchUsers.usecase.impl;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.user.searchUsers.usecase.SearchUsersUseCase;
import co.edu.uco.ucochallenge.domain.pagination.PaginatedResult;
import co.edu.uco.ucochallenge.domain.user.model.User;
import co.edu.uco.ucochallenge.domain.user.model.UserSearchQuery;
import co.edu.uco.ucochallenge.domain.user.port.out.UserRepository;

@Service
public class SearchUsersUseCaseImpl implements SearchUsersUseCase {

        private final UserRepository repository;

        public SearchUsersUseCaseImpl(final UserRepository repository) {
                this.repository = repository;
        }

        @Override
        public PaginatedResult<User> execute(final UserSearchQuery domain) {
                return repository.findByFilter(domain.filter(), domain.pagination());
        }
}
