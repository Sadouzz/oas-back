package sn.oas.facturation.features.client.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.oas.facturation.features.client.data.entity.PortefeuilleClient;
import java.util.Optional;

public interface PortefeuilleClientRepository extends JpaRepository<PortefeuilleClient, Long> {
    Optional<PortefeuilleClient> findByClientIdAndGarageId(Long clientId, Long garageId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PortefeuilleClient p WHERE p.client.id = :clientId AND p.garage.id = :garageId")
    Optional<PortefeuilleClient> findByClientIdForUpdate(@Param("clientId") Long clientId, @Param("garageId") Long garageId);
}
