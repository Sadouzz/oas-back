package sn.oas.facturation.features.diagnostic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.diagnostic.data.entity.RemarqueDiagnostic;

import java.util.List;

@Repository
public interface RemarqueDiagnosticRepository extends JpaRepository<RemarqueDiagnostic, Long> {
    List<RemarqueDiagnostic> findByDiagnosticIdOrderByCreatedAtDesc(Long diagnosticId);
    List<RemarqueDiagnostic> findByDiagnosticOrdreReparationIdOrderByCreatedAtDesc(Long ordreReparationId);
}
