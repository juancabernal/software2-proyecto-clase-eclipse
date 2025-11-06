package co.edu.uco.ucochallenge.secondary.adapters.cache.redis;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import co.edu.uco.ucochallenge.application.user.search.port.UserSearchQueryRepositoryPort;
import co.edu.uco.ucochallenge.domain.user.search.model.UserSearchResultDomainModel;

/** Decorator que agrega caché a findAll(page,size) */
@Repository
public class UsersCacheRepositoryAdapter implements UserSearchQueryRepositoryPort {

    private final UserSearchQueryRepositoryPort delegate;

    public UsersCacheRepositoryAdapter(UserSearchQueryRepositoryPort delegate) {
        this.delegate = delegate;
    }

    @Override
    @Cacheable(cacheNames = "usersByPage", keyGenerator = "usersPageKeyGenerator")
    public UserSearchResultDomainModel findAll(int page, int size) {
        return delegate.findAll(page, size);
    }
}
