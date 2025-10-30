package co.edu.uco.ucochallenge.infrastructure.secondary.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

        @Query("""
                        SELECT u FROM UserEntity u
                        WHERE (:idType IS NULL OR u.idType.id = :idType)
                        AND (:homeCity IS NULL OR u.homeCity.id = :homeCity)
                        AND (:idNumber IS NULL OR LOWER(u.idNumber) LIKE LOWER(CONCAT('%', :idNumber, '%')))
                        AND (:firstName IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')))
                        AND (:firstSurname IS NULL OR LOWER(u.firstSurname) LIKE LOWER(CONCAT('%', :firstSurname, '%')))
                        AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
                        AND (:mobileNumber IS NULL OR u.mobileNumber LIKE CONCAT('%', :mobileNumber, '%'))
                        """)
        List<UserEntity> search(
                        @Param("idType") UUID idType,
                        @Param("homeCity") UUID homeCity,
                        @Param("idNumber") String idNumber,
                        @Param("firstName") String firstName,
                        @Param("firstSurname") String firstSurname,
                        @Param("email") String email,
                        @Param("mobileNumber") String mobileNumber);
}
