package sn.oas.facturation.features.facture.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.facture.data.enums.StatutPaiement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long>, JpaSpecificationExecutor<Facture> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Facture f WHERE f.id = :id")
    java.util.Optional<Facture> findByIdForPaiement(@Param("id") Long id);

    @Query("SELECT f FROM Facture f LEFT JOIN f.client c LEFT JOIN f.vehicule v "
            + "LEFT JOIN f.ordreReparation o LEFT JOIN o.client oc LEFT JOIN o.vehicule ov LEFT JOIN ov.client ovc LEFT JOIN v.client vc WHERE "
            + "LOWER(COALESCE(f.numero, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(f.remarque, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(f.numeroBonDeCommande, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(o.numero, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(v.immatriculation, ov.immatriculation, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(v.marque, ov.marque, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(v.modele, ov.modele, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(c.firstName, oc.firstName, ovc.firstName, vc.firstName, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(c.lastName, oc.lastName, ovc.lastName, vc.lastName, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(c.matricule, oc.matricule, ovc.matricule, vc.matricule, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(c.codeClient, oc.codeClient, ovc.codeClient, vc.codeClient, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "(:phoneDigits <> '' AND REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(c.phone, oc.phone, ovc.phone, vc.phone, ''), '+', ''), ' ', ''), '-', ''), '(', ''), ')', ''), '.', '') LIKE CONCAT('%', :phoneDigits, '%'))")
    List<Facture> searchFactures(@Param("keyword") String keyword, @Param("phoneDigits") String phoneDigits);

    @Query("SELECT f FROM Facture f LEFT JOIN f.client c LEFT JOIN f.vehicule v "
            + "LEFT JOIN f.ordreReparation o LEFT JOIN o.client oc LEFT JOIN o.vehicule ov LEFT JOIN ov.client ovc LEFT JOIN v.client vc WHERE "
            + "LOWER(COALESCE(f.numero, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(f.remarque, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(f.numeroBonDeCommande, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(o.numero, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(v.immatriculation, ov.immatriculation, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(v.marque, ov.marque, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(v.modele, ov.modele, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(c.firstName, oc.firstName, ovc.firstName, vc.firstName, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(c.lastName, oc.lastName, ovc.lastName, vc.lastName, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(c.matricule, oc.matricule, ovc.matricule, vc.matricule, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(COALESCE(c.codeClient, oc.codeClient, ovc.codeClient, vc.codeClient, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "(:phoneDigits <> '' AND REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(c.phone, oc.phone, ovc.phone, vc.phone, ''), '+', ''), ' ', ''), '-', ''), '(', ''), ')', ''), '.', '') LIKE CONCAT('%', :phoneDigits, '%'))")
    org.springframework.data.domain.Page<Facture> searchFactures(@Param("keyword") String keyword,
            @Param("phoneDigits") String phoneDigits, org.springframework.data.domain.Pageable pageable);

    List<Facture> findTop5ByOrderByDateCreationDesc();
    List<Facture> findByClientIdOrderByDateCreationDesc(Long clientId);

    @Query("SELECT DISTINCT f FROM Facture f LEFT JOIN f.client fc LEFT JOIN f.ordreReparation o LEFT JOIN o.client oc "
            + "LEFT JOIN f.vehicule v LEFT JOIN v.client vc "
            + "WHERE fc.id = :clientId OR oc.id = :clientId OR vc.id = :clientId "
            + "ORDER BY f.dateCreation DESC")
    List<Facture> findFacturesVisiblesParClient(@Param("clientId") Long clientId);

    @Query("SELECT f FROM Facture f LEFT JOIN f.client fc LEFT JOIN f.ordreReparation o LEFT JOIN o.client oc "
            + "LEFT JOIN f.vehicule v LEFT JOIN v.client vc "
            + "WHERE f.id = :factureId AND (fc.id = :clientId OR oc.id = :clientId OR vc.id = :clientId)")
    java.util.Optional<Facture> findFactureVisibleParClient(@Param("clientId") Long clientId,
            @Param("factureId") Long factureId);

    @Query("SELECT COALESCE(SUM(f.resteAPayer), 0) FROM Facture f WHERE f.client.id = :clientId")
    java.math.BigDecimal sumResteAPayerByClientId(@Param("clientId") Long clientId);

    @Query("SELECT COALESCE(SUM(f.montantTotal), 0) FROM Facture f WHERE f.client.id = :clientId AND f.dateCreation >= :debut")
    java.math.BigDecimal sumMontantFacturesDepuis(@Param("clientId") Long clientId, @Param("debut") java.time.LocalDateTime debut);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Facture f WHERE f.client.id = :clientId AND f.resteAPayer > 0 AND f.dateEcheance IS NOT NULL AND f.dateEcheance < :maintenant")
    boolean existsFactureImpayeeEnRetard(@Param("clientId") Long clientId, @Param("maintenant") java.time.LocalDateTime maintenant);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Facture f WHERE f.client.id = :clientId AND f.resteAPayer > 0 AND f.dateEcheance IS NULL AND f.dateCreation < :limite")
    boolean existsFactureLegacyImpayeeEnRetard(@Param("clientId") Long clientId, @Param("limite") java.time.LocalDateTime limite);

    java.util.Optional<Facture> findFirstByOrdreReparationId(Long ordreReparationId);
    java.util.Optional<Facture> findFirstByProformaId(Long proformaId);
}
