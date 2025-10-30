package co.edu.uco.ucochallenge.application.user.searchUsers.usecase;

import java.util.List;

import co.edu.uco.ucochallenge.application.usecase.UseCase;
import co.edu.uco.ucochallenge.domain.user.model.User;
import co.edu.uco.ucochallenge.domain.user.model.UserFilter;

public interface SearchUsersUseCase extends UseCase<UserFilter, List<User>> {
}
