package co.edu.uco.ucochallenge.application.user.searchUsers.usecase;

import co.edu.uco.ucochallenge.application.usecase.UseCase;
import co.edu.uco.ucochallenge.domain.pagination.PaginatedResult;
import co.edu.uco.ucochallenge.domain.user.model.User;
import co.edu.uco.ucochallenge.domain.user.model.UserSearchQuery;

public interface SearchUsersUseCase extends UseCase<UserSearchQuery, PaginatedResult<User>> {
}
