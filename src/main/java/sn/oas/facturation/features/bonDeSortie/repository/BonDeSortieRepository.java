package sn.oas.facturation.features.bonDeSortie.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortie;
import sn.oas.facturation.features.bonDeSortie.data.enums.StatutBon;
import sn.oas.facturation.features.bonDeSortie.dto.BonDeSortieDisponibleResponse;

import java.util.List;
import java.util.Optional;

@Repository
public interface BonDeSortieRepository extends JpaRepository<BonDeSortie, Long> {

    boolean existsByOrdreReparationId(Long ordreReparationId);
    boolean existsByOrdreTravailId(Long ordreTravailId);
    Optional<BonDeSortie> findFirstByOrdreReparationIdOrderByIdDesc(Long ordreReparationId);
    Optional<BonDeSortie> findFirstByOrdreTravailIdOrderByIdDesc(Long ordreTravailId);

    Optional<BonDeSortie> findByReference(String reference);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BonDeSortie b WHERE b.id = :id")
    Optional<BonDeSortie> findByIdForUpdate(@Param("id") Long id);
    List<BonDeSortie> findByProformaId(Long proformaId);
    Optional<BonDeSortie> findTopByProformaIdOrderByIdDesc(Long proformaId);
    List<BonDeSortie> findByOrdreReparationIdOrderByDateDesc(Long ordreReparationId);
    List<BonDeSortie> findByOrdreReparationIdAndStatutOrderByDateDesc(Long ordreReparationId, StatutBon statut);
    @Query("SELECT new sn.oas.facturation.features.bonDeSortie.dto.BonDeSortieDisponibleResponse(" +
            "b.id, b.reference, b.date, b.montantTotal, b.client.id, b.vehicule.id, b.ordreReparation.id) " +
            "FROM BonDeSortie b WHERE b.ordreReparation.id = :ordreReparationId AND b.statut = :statut ORDER BY b.date DESC")
    List<BonDeSortieDisponibleResponse> trouverDisponiblesParOrdreEtStatut(
            @Param("ordreReparationId") Long ordreReparationId, @Param("statut") StatutBon statut);
    List<BonDeSortie> findByOrdreTravailIdOrderByDateDesc(Long ordreTravailId);
    Page<BonDeSortie> findByOrdreReparationIdOrderByDateDesc(Long ordreReparationId, Pageable pageable);
    Page<BonDeSortie> findByOrdreTravailIdOrderByDateDesc(Long ordreTravailId, Pageable pageable);

    List<BonDeSortie> findByStatutOrderByDateDesc(StatutBon statut);
    Page<BonDeSortie> findByStatut(StatutBon statut, Pageable pageable);
    long countByStatut(StatutBon statut);

    @Query("SELECT ligne.piece.id AS pieceId, ligne.designation AS designation, SUM(ligne.quantite) AS quantiteTotale " +
            "FROM BonDeSortie bon JOIN bon.lignesBonDeSortiePieces ligne " +
            "WHERE bon.ordreReparation.id = :ordreReparationId AND bon.statut = :statut " +
            "GROUP BY ligne.piece.id, ligne.designation")
    List<PieceQuantiteProjection> sommerQuantitesPiecesValideesParOrdreReparation(
            @Param("ordreReparationId") Long ordreReparationId, @Param("statut") StatutBon statut);

    @Query("SELECT ligne.mainDoeuvre.id AS mainDoeuvreId, ligne.description AS designation, SUM(ligne.nbreHeure) AS quantiteTotale " +
            "FROM BonDeSortie bon JOIN bon.lignesBonDeSortieMainDoeuvres ligne " +
            "WHERE bon.ordreReparation.id = :ordreReparationId AND bon.statut = :statut " +
            "GROUP BY ligne.mainDoeuvre.id, ligne.description")
    List<MainDoeuvreQuantiteProjection> sommerQuantitesMainDoeuvreValideesParOrdreReparation(
            @Param("ordreReparationId") Long ordreReparationId, @Param("statut") StatutBon statut);

    interface PieceQuantiteProjection {
        Long getPieceId();
        String getDesignation();
        Long getQuantiteTotale();
    }

    interface MainDoeuvreQuantiteProjection {
        Long getMainDoeuvreId();
        String getDesignation();
        Long getQuantiteTotale();
    }

    List<BonDeSortie> findByClientIdOrderByDateDesc(Long clientId);
    Page<BonDeSortie> findByClientId(Long clientId, Pageable pageable);

    List<BonDeSortie> findByVehiculeIdOrderByDateDesc(Long vehiculeId);
    Page<BonDeSortie> findByVehiculeId(Long vehiculeId, Pageable pageable);

    Page<BonDeSortie> findByOrdreTravailId(Long ordreTravailId, Pageable pageable);
    Page<BonDeSortie> findByOrdreReparationId(Long ordreReparationId, Pageable pageable);

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
