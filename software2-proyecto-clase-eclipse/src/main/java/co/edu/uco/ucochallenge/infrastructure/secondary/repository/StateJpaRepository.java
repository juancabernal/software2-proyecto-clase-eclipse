package co.edu.uco.ucochallenge.infrastructure.secondary.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.uco.ucochallenge.infrastructure.secondary.repository.entity.StateEntity;

@Repository
public interface StateJpaRepository extends JpaRepository<StateEntity, UUID> {
    List<StateEntity> findAllByOrderByNameAsc();
}
