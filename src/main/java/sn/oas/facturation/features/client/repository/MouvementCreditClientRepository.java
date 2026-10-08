package sn.oas.facturation.features.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import sn.oas.facturation.features.client.data.entity.MouvementCreditClient;

public interface MouvementCreditClientRepository extends JpaRepository<MouvementCreditClient, Long> {
    List<MouvementCreditClient> findTop100ByCompteClientIdOrderByDateCreationDesc(Long compteClientId);
}
