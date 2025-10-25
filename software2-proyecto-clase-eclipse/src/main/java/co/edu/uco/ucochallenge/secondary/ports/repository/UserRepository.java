package co.edu.uco.ucochallenge.secondary.ports.repository;
import co.edu.uco.ucochallenge.secondary.adapters.repository.entity.*;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

}
