package sn.oas.facturation.features.diagnostic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.diagnostic.data.entity.Diagnostic;
import sn.oas.facturation.features.diagnostic.data.enums.StatutDiagnostic;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiagnosticRepository extends JpaRepository<Diagnostic, Long> {

    @Query("SELECT d FROM Diagnostic d " +
            "LEFT JOIN FETCH d.ordreReparation or_ " +
            "LEFT JOIN FETCH or_.vehicule v " +
            "LEFT JOIN FETCH v.client c " +
            "LEFT JOIN FETCH d.technicien t " +
            "WHERE d.id = :id")
    Optional<Diagnostic> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT d FROM Diagnostic d " +
            "LEFT JOIN FETCH d.ordreReparation or_ " +
            "LEFT JOIN FETCH or_.vehicule v " +
            "LEFT JOIN FETCH v.client c " +
            "LEFT JOIN FETCH d.technicien t " +
            "WHERE or_.id = :ordreReparationId")
    Optional<Diagnostic> findByOrdreReparationId(@Param("ordreReparationId") Long ordreReparationId);

    boolean existsByOrdreReparationId(Long ordreReparationId);

    List<Diagnostic> findByTechnicienId(Long technicienId);

    @Query(value = "SELECT d FROM Diagnostic d " +
            "LEFT JOIN FETCH d.ordreReparation or_ " +
            "LEFT JOIN FETCH or_.vehicule v " +
            "LEFT JOIN FETCH v.client c " +
            "LEFT JOIN FETCH d.technicien t",
            countQuery = "SELECT COUNT(d) FROM Diagnostic d")
    Page<Diagnostic> findAllWithDetails(Pageable pageable);

    @Query(value = "SELECT d FROM Diagnostic d " +
            "LEFT JOIN FETCH d.ordreReparation or_ " +
            "LEFT JOIN FETCH or_.vehicule v " +
            "LEFT JOIN FETCH v.client c " +
            "LEFT JOIN FETCH d.technicien t " +
            "WHERE d.statut = :statut",
            countQuery = "SELECT COUNT(d) FROM Diagnostic d WHERE d.statut = :statut")
    Page<Diagnostic> findByStatutWithDetails(@Param("statut") StatutDiagnostic statut, Pageable pageable);

    @Query(value = "SELECT d FROM Diagnostic d " +
            "LEFT JOIN FETCH d.ordreReparation or_ " +
            "LEFT JOIN FETCH or_.vehicule v " +
            "LEFT JOIN FETCH v.client c " +
            "LEFT JOIN FETCH d.technicien t " +
            "WHERE LOWER(d.observations) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.pannesDetectees) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.recommandations) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(or_.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))",
            countQuery = "SELECT COUNT(d) FROM Diagnostic d LEFT JOIN d.ordreReparation or_ LEFT JOIN or_.vehicule v LEFT JOIN v.client c " +
            "WHERE LOWER(d.observations) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.pannesDetectees) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.recommandations) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(or_.numero) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.marque) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.modele) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Diagnostic> searchDiagnostics(@Param("keyword") String keyword, Pageable pageable);
}
