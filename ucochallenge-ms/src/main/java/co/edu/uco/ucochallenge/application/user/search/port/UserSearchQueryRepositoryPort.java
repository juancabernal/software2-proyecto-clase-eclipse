package co.edu.uco.ucochallenge.application.user.search.port;

import co.edu.uco.ucochallenge.domain.user.search.model.UserSearchResultDomainModel;

public interface UserSearchQueryRepositoryPort {

        UserSearchResultDomainModel findAll(int page, int size);
}
