package sn.oas.facturation.features.diagnostic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.diagnostic.data.entity.PieceJointeDiagnostic;
import sn.oas.facturation.features.diagnostic.data.enums.TypePieceJointe;

import java.util.List;

@Repository
public interface PieceJointeDiagnosticRepository extends JpaRepository<PieceJointeDiagnostic, Long> {
    List<PieceJointeDiagnostic> findByDiagnosticIdOrderByCreatedAtDesc(Long diagnosticId);
    List<PieceJointeDiagnostic> findByDiagnosticIdAndTypeOrderByCreatedAtDesc(Long diagnosticId, TypePieceJointe type);
    List<PieceJointeDiagnostic> findByDiagnosticOrdreReparationIdOrderByCreatedAtDesc(Long ordreReparationId);
    List<PieceJointeDiagnostic> findByDiagnosticOrdreReparationIdAndTypeOrderByCreatedAtDesc(Long ordreReparationId, TypePieceJointe type);
}
