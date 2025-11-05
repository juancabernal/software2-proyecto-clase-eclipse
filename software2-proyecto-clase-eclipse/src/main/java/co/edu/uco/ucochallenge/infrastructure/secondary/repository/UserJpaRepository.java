package co.edu.uco.ucochallenge.infrastructure.secondary.repository;

import java.util.UUID;


import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import co.edu.uco.ucochallenge.infrastructure.secondary.repository.entity.UserEntity;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

        boolean existsByEmailIgnoreCase(String email);

        boolean existsByIdTypeIdAndIdNumber(UUID idType, String idNumber);

        boolean existsByMobileNumber(String mobileNumber);

        boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

        boolean existsByIdTypeIdAndIdNumberAndIdNot(UUID idType, String idNumber, UUID id);

        boolean existsByMobileNumberAndIdNot(String mobileNumber, UUID id);

}
