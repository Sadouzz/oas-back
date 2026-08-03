package sn.oas.facturation.marketplace.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.marketplace.data.entity.DemandeProduit;

import java.util.List;

@Repository
public interface DemandeProduitRepository extends JpaRepository<DemandeProduit, Long> {
    List<DemandeProduit> findByClientOrderByDateCreationDesc(Client client);

    java.util.Optional<DemandeProduit> findByIdAndClient(Long id, Client client);

    List<DemandeProduit> findByProduitIdOrderByDateCreationDesc(Long produitId);

    List<DemandeProduit> findAllByOrderByDateCreationDesc();

    long countByStatut(sn.oas.facturation.marketplace.data.enums.StatutDemandeProduit statut);
}
