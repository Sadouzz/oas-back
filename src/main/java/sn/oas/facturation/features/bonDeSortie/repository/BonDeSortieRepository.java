package sn.oas.facturation.features.bonDeSortie.repository;

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

    List<BonDeSortie> findByClientIdOrderByDateDesc(Long clientId);

    List<BonDeSortie> findByVehiculeIdOrderByDateDesc(Long vehiculeId);

    @Query("SELECT COUNT(b) FROM BonDeSortie b WHERE YEAR(b.date) = :year")
    long countByAnnee(@Param("year") int year);

    @Query("SELECT b FROM BonDeSortie b WHERE " +
            "LOWER(b.reference) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.remarque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.vehicule.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.vehicule.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.vehicule.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.client.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.client.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<BonDeSortie> searchBonsDeSortie(@Param("keyword") String keyword);
}