package sn.oas.facturation.features.avoirTTC.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.avoirTTC.data.entity.AvoirTTC;

import java.util.List;

@Repository
public interface AvoirTTCRepository extends JpaRepository<AvoirTTC, Long> {

    @Query(value = "SELECT a FROM AvoirTTC a " +
            "LEFT JOIN a.vehicule v " +
            "LEFT JOIN a.client c " +
            "LEFT JOIN a.ordreReparation o " +
            "LEFT JOIN o.vehicule ov " +
            "LEFT JOIN ov.client ovc " +
            "WHERE LOWER(a.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.remarque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ov.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ovc.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ovc.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(CONCAT(ovc.firstName, ' ', ovc.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<AvoirTTC> searchAvoirsTTC(@Param("keyword") String keyword);

    @Query(value = "SELECT a FROM AvoirTTC a " +
            "LEFT JOIN a.vehicule v " +
            "LEFT JOIN a.client c " +
            "LEFT JOIN a.ordreReparation o " +
            "LEFT JOIN o.vehicule ov " +
            "LEFT JOIN ov.client ovc " +
            "WHERE LOWER(a.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.remarque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ov.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ovc.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ovc.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(CONCAT(ovc.firstName, ' ', ovc.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%'))",
           countQuery = "SELECT COUNT(a) FROM AvoirTTC a " +
            "LEFT JOIN a.vehicule v " +
            "LEFT JOIN a.client c " +
            "LEFT JOIN a.ordreReparation o " +
            "LEFT JOIN o.vehicule ov " +
            "LEFT JOIN ov.client ovc " +
            "WHERE LOWER(a.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.remarque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ov.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ovc.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ovc.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(CONCAT(ovc.firstName, ' ', ovc.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    org.springframework.data.domain.Page<AvoirTTC> searchAvoirsTTC(@Param("keyword") String keyword, org.springframework.data.domain.Pageable pageable);

    List<AvoirTTC> findTop5ByOrderByDateCreationDesc();

    @Query("SELECT DISTINCT a FROM AvoirTTC a "
            + "LEFT JOIN FETCH a.client c "
            + "LEFT JOIN FETCH a.vehicule v "
            + "LEFT JOIN FETCH v.client vc "
            + "LEFT JOIN FETCH a.ordreReparation o "
            + "LEFT JOIN FETCH o.client oc "
            + "LEFT JOIN FETCH o.vehicule ov "
            + "LEFT JOIN FETCH ov.client ovc "
            + "WHERE a.dateCreation >= :debut AND a.dateCreation < :finExclusive "
            + "ORDER BY a.dateCreation DESC, a.id DESC")
    List<AvoirTTC> findHistoriqueBetween(
            @Param("debut") java.time.LocalDateTime debut,
            @Param("finExclusive") java.time.LocalDateTime finExclusive);

    org.springframework.data.domain.Page<AvoirTTC> findByDateCreationGreaterThanEqualAndDateCreationLessThanOrderByDateCreationDescIdDesc(
            java.time.LocalDateTime debut,
            java.time.LocalDateTime finExclusive,
            org.springframework.data.domain.Pageable pageable);

    List<AvoirTTC> findByDateCreationGreaterThanEqualAndDateCreationLessThanOrderByDateCreationDescIdDesc(
            java.time.LocalDateTime debut,
            java.time.LocalDateTime finExclusive);

    @Query("SELECT COALESCE(SUM(a.montantTVA), 0) FROM AvoirTTC a "
            + "WHERE a.dateCreation >= :debut AND a.dateCreation < :finExclusive")
    java.math.BigDecimal sumMontantTvaBetween(
            @Param("debut") java.time.LocalDateTime debut,
            @Param("finExclusive") java.time.LocalDateTime finExclusive);

    @Query("SELECT COALESCE(SUM(a.montantTimbre), 0) FROM AvoirTTC a "
            + "WHERE a.dateCreation >= :debut AND a.dateCreation < :finExclusive")
    java.math.BigDecimal sumMontantTimbreBetween(
            @Param("debut") java.time.LocalDateTime debut,
            @Param("finExclusive") java.time.LocalDateTime finExclusive);
}
