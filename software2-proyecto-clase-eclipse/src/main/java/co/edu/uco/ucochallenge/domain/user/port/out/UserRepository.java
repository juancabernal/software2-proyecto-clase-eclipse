package co.edu.uco.ucochallenge.domain.user.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

        List<User> findAll();

        Optional<User> findById(UUID id);

        void deleteById(UUID id);

        List<User> findByFilter(UserFilter filter);
}
