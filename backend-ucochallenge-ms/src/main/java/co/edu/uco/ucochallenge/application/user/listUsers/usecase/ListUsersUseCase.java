package co.edu.uco.ucochallenge.application.user.listUsers.usecase;

import co.edu.uco.ucochallenge.application.usecase.UseCase;
import co.edu.uco.ucochallenge.domain.pagination.PageCriteria;
import co.edu.uco.ucochallenge.domain.pagination.PaginatedResult;
import co.edu.uco.ucochallenge.domain.user.model.User;

public interface ListUsersUseCase extends UseCase<PageCriteria, PaginatedResult<User>> {
}
