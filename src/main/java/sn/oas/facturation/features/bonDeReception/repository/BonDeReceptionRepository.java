package sn.oas.facturation.features.bonDeReception.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.bonDeReception.data.entity.BonDeReception;

import java.util.List;

@Repository
public interface BonDeReceptionRepository extends JpaRepository<BonDeReception, Long> {

    @Query("SELECT b FROM BonDeReception b WHERE " +
            "LOWER(b.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.remarque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.ordreReparation.vehicule.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.ordreReparation.vehicule.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.ordreReparation.vehicule.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.ordreReparation.vehicule.client.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.ordreReparation.vehicule.client.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<BonDeReception> searchBonsDeReception(@Param("keyword") String keyword);

    @Query("SELECT b FROM BonDeReception b WHERE " +
            "LOWER(b.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.remarque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.ordreReparation.vehicule.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.ordreReparation.vehicule.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.ordreReparation.vehicule.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.ordreReparation.vehicule.client.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.ordreReparation.vehicule.client.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    org.springframework.data.domain.Page<BonDeReception> searchBonsDeReception(@Param("keyword") String keyword, org.springframework.data.domain.Pageable pageable);

    List<BonDeReception> findTop5ByOrderByDateCreationDesc();
}
