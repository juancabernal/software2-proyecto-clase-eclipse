package co.edu.uco.ucochallenge.domain.user.port.out;

import java.util.Optional;
import java.util.UUID;

import co.edu.uco.ucochallenge.domain.pagination.PageCriteria;
import co.edu.uco.ucochallenge.domain.pagination.PaginatedResult;
import co.edu.uco.ucochallenge.domain.user.model.User;
import co.edu.uco.ucochallenge.domain.user.model.UserFilter;

public interface UserRepository {

    boolean existsByEmail(String email);

    boolean existsByIdTypeAndIdNumber(UUID idType, String idNumber);

    boolean existsByMobileNumber(String mobileNumber);

    boolean existsByEmailExcludingId(UUID id, String email);

    boolean existsByIdTypeAndIdNumberExcludingId(UUID id, UUID idType, String idNumber);

    boolean existsByMobileNumberExcludingId(UUID id, String mobileNumber);

    User save(User user);

    PaginatedResult<User> findAll(PageCriteria pagination);

    Optional<User> findById(UUID id);

    void deleteById(UUID id);

    PaginatedResult<User> findByFilter(UserFilter filter, PageCriteria pagination);
}
