package co.edu.uco.ucochallenge.infrastructure.secondary.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.uco.ucochallenge.infrastructure.secondary.repository.entity.CityEntity;

@Repository
public interface CityJpaRepository extends JpaRepository<CityEntity, UUID> {
}
