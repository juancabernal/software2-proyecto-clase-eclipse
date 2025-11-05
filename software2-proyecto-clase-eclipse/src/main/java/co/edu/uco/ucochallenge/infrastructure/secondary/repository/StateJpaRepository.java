package co.edu.uco.ucochallenge.infrastructure.secondary.repository;

import java.util.List;
import java.util.UUID;

import co.edu.uco.ucochallenge.infrastructure.secondary.repository.entity.StateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StateJpaRepository extends JpaRepository<StateEntity, UUID> {
    List<StateEntity> findAllByOrderByNameAsc();
}
