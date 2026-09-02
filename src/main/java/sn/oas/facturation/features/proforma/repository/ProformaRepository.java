package sn.oas.facturation.features.proforma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.proforma.data.entity.Proforma;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProformaRepository extends JpaRepository<Proforma, Long> {

    @Query("SELECT p FROM Proforma p WHERE " +
            "LOWER(p.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.remarque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.bonDeCommande.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.ordreReparation.vehicule.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.ordreReparation.vehicule.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.ordreReparation.vehicule.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.ordreReparation.vehicule.client.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.ordreReparation.vehicule.client.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Proforma> searchProformas(@Param("keyword") String keyword);

    @Query("SELECT p FROM Proforma p WHERE " +
            "LOWER(p.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.remarque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.bonDeCommande.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.ordreReparation.vehicule.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.ordreReparation.vehicule.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.ordreReparation.vehicule.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.ordreReparation.vehicule.client.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.ordreReparation.vehicule.client.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    org.springframework.data.domain.Page<Proforma> searchProformas(@Param("keyword") String keyword, org.springframework.data.domain.Pageable pageable);

    List<Proforma> findTop5ByOrderByDateCreationDesc();

    Optional<Proforma> findByOrdreReparationId(Long ordreReparationId);

    @Query("SELECT p FROM Proforma p WHERE p.ordreReparation.vehicule.client.id = :clientId " +
            "AND p.visibleClient = true ORDER BY p.dateCreation DESC")
    List<Proforma> findByClientIdOrderByDateCreationDesc(@Param("clientId") Long clientId);
}
