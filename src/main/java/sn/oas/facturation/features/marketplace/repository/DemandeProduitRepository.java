package sn.oas.facturation.features.marketplace.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.marketplace.data.entity.DemandeProduit;
import sn.oas.facturation.features.marketplace.data.enums.StatutDemandeProduit;

import java.util.List;

@Repository
public interface DemandeProduitRepository extends JpaRepository<DemandeProduit, Long> {
    List<DemandeProduit> findByClientOrderByDateCreationDesc(Client client);

    java.util.Optional<DemandeProduit> findByIdAndClient(Long id, Client client);

    List<DemandeProduit> findByProduitIdOrderByDateCreationDesc(Long produitId);

    List<DemandeProduit> findAllByOrderByDateCreationDesc();

    long countByStatut(StatutDemandeProduit statut);
}
