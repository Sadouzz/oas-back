package sn.oas.facturation.ordreReparation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.ordreReparation.data.entity.PieceJointeDiagnostic;
import sn.oas.facturation.ordreReparation.data.enums.TypePieceJointe;

import java.util.List;

@Repository
public interface PieceJointeDiagnosticRepository extends JpaRepository<PieceJointeDiagnostic, Long> {
    List<PieceJointeDiagnostic> findByOrdreReparationIdOrderByCreatedAtDesc(Long ordreReparationId);
    List<PieceJointeDiagnostic> findByOrdreReparationIdAndTypeOrderByCreatedAtDesc(Long ordreReparationId, TypePieceJointe type);
}
