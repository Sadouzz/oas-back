package sn.oas.facturation.features.diagnostic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.oas.facturation.features.diagnostic.data.entity.RemarqueDiagnostic;

import java.util.List;

public interface RemarqueDiagnosticRepository extends JpaRepository<RemarqueDiagnostic, Long> {
    List<RemarqueDiagnostic> findByOrdreReparationIdOrderByCreatedAtDesc(Long ordreReparationId);
}
