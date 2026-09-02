package sn.oas.facturation.features.facture.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.facture.data.entity.Facture;

import java.util.List;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {

    @Query("SELECT f FROM Facture f WHERE " +
            "LOWER(f.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.remarque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.numeroBonDeCommande) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.ordreReparation.vehicule.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.ordreReparation.vehicule.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.ordreReparation.vehicule.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.ordreReparation.vehicule.client.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.ordreReparation.vehicule.client.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Facture> searchFactures(@Param("keyword") String keyword);

    @Query("SELECT f FROM Facture f WHERE " +
            "LOWER(f.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.remarque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.numeroBonDeCommande) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.ordreReparation.vehicule.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.ordreReparation.vehicule.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.ordreReparation.vehicule.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.ordreReparation.vehicule.client.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.ordreReparation.vehicule.client.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    org.springframework.data.domain.Page<Facture> searchFactures(@Param("keyword") String keyword, org.springframework.data.domain.Pageable pageable);

    List<Facture> findTop5ByOrderByDateCreationDesc();
    List<Facture> findByClientIdOrderByDateCreationDesc(Long clientId);
}
