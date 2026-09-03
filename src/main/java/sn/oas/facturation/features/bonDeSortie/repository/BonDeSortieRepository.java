package sn.oas.facturation.features.bonDeSortie.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortie;
import sn.oas.facturation.features.bonDeSortie.data.enums.StatutBon;

import java.util.List;
import java.util.Optional;

@Repository
public interface BonDeSortieRepository extends JpaRepository<BonDeSortie, Long> {

    Optional<BonDeSortie> findByReference(String reference);

    List<BonDeSortie> findByStatutOrderByDateDesc(StatutBon statut);
    Page<BonDeSortie> findByStatut(StatutBon statut, Pageable pageable);
    long countByStatut(StatutBon statut);

    List<BonDeSortie> findByClientIdOrderByDateDesc(Long clientId);
    Page<BonDeSortie> findByClientId(Long clientId, Pageable pageable);

    List<BonDeSortie> findByVehiculeIdOrderByDateDesc(Long vehiculeId);
    Page<BonDeSortie> findByVehiculeId(Long vehiculeId, Pageable pageable);

    @Query("SELECT COUNT(b) FROM BonDeSortie b WHERE YEAR(b.date) = :year")
    long countByAnnee(@Param("year") int year);

    @Query("SELECT b FROM BonDeSortie b WHERE " +
            "LOWER(b.reference) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "(b.remarque IS NOT NULL AND LOWER(b.remarque) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(b.vehicule.immatriculation IS NOT NULL AND LOWER(b.vehicule.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(b.vehicule.marque IS NOT NULL AND LOWER(b.vehicule.marque) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(b.vehicule.modele IS NOT NULL AND LOWER(b.vehicule.modele) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(b.client.firstName IS NOT NULL AND LOWER(b.client.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(b.client.lastName IS NOT NULL AND LOWER(b.client.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<BonDeSortie> searchBonsDeSortie(@Param("keyword") String keyword);

    @Query("SELECT b FROM BonDeSortie b WHERE " +
            "LOWER(b.reference) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "(b.remarque IS NOT NULL AND LOWER(b.remarque) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(b.vehicule.immatriculation IS NOT NULL AND LOWER(b.vehicule.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(b.vehicule.marque IS NOT NULL AND LOWER(b.vehicule.marque) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(b.vehicule.modele IS NOT NULL AND LOWER(b.vehicule.modele) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(b.client.firstName IS NOT NULL AND LOWER(b.client.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(b.client.lastName IS NOT NULL AND LOWER(b.client.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<BonDeSortie> searchBonsDeSortie(@Param("keyword") String keyword, Pageable pageable);
}