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

    @Query(value = "SELECT p FROM Proforma p " +
            "LEFT JOIN p.bonDeCommande b " +
            "LEFT JOIN p.ordreReparation o " +
            "LEFT JOIN o.vehicule v " +
            "LEFT JOIN v.client c " +
            "WHERE LOWER(p.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.remarque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Proforma> searchProformas(@Param("keyword") String keyword);

    @Query(value = "SELECT p FROM Proforma p " +
            "LEFT JOIN p.bonDeCommande b " +
            "LEFT JOIN p.ordreReparation o " +
            "LEFT JOIN o.vehicule v " +
            "LEFT JOIN v.client c " +
            "WHERE LOWER(p.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.remarque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))",
           countQuery = "SELECT COUNT(p) FROM Proforma p " +
            "LEFT JOIN p.bonDeCommande b " +
            "LEFT JOIN p.ordreReparation o " +
            "LEFT JOIN o.vehicule v " +
            "LEFT JOIN v.client c " +
            "WHERE LOWER(p.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.remarque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    org.springframework.data.domain.Page<Proforma> searchProformas(@Param("keyword") String keyword, org.springframework.data.domain.Pageable pageable);

    List<Proforma> findTop5ByOrderByDateCreationDesc();

    Optional<Proforma> findByOrdreReparationId(Long ordreReparationId);

    @Query("SELECT p FROM Proforma p WHERE p.ordreReparation.vehicule.client.id = :clientId " +
            "AND p.visibleClient = true ORDER BY p.dateCreation DESC")
    List<Proforma> findByClientIdOrderByDateCreationDesc(@Param("clientId") Long clientId);

    @Query("SELECT COUNT(p) FROM Proforma p WHERE p.ordreReparation.vehicule.client.id = :clientId " +
            "AND p.visibleClient = true AND (p.statut IS NULL OR p.statut = :statut)")
    long countByClientIdAndStatutEnAttente(@Param("clientId") Long clientId, @Param("statut") sn.oas.facturation.features.facturation.data.enums.StatutFacturation statut);
}
