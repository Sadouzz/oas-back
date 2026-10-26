package sn.oas.facturation.features.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.oas.facturation.features.client.data.entity.MouvementPortefeuilleClient;
import java.util.List;

public interface MouvementPortefeuilleClientRepository extends JpaRepository<MouvementPortefeuilleClient, Long> {
    List<MouvementPortefeuilleClient> findTop100ByPortefeuilleIdOrderByDateCreationDesc(Long portefeuilleId);
}
