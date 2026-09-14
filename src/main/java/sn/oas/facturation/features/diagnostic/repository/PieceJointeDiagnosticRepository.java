package sn.oas.facturation.features.diagnostic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.diagnostic.data.entity.PieceJointeDiagnostic;
import sn.oas.facturation.features.diagnostic.data.enums.TypePieceJointe;

import java.util.List;

@Repository
public interface PieceJointeDiagnosticRepository extends JpaRepository<PieceJointeDiagnostic, Long> {
    List<PieceJointeDiagnostic> findByOrdreReparationIdOrderByCreatedAtDesc(Long ordreReparationId);
    List<PieceJointeDiagnostic> findByOrdreReparationIdAndTypeOrderByCreatedAtDesc(Long ordreReparationId, TypePieceJointe type);
}
