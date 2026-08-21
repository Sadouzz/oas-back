package sn.oas.facturation.ordreReparation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.oas.facturation.ordreReparation.data.entity.RemarqueDiagnostic;

import java.util.List;

public interface RemarqueDiagnosticRepository extends JpaRepository<RemarqueDiagnostic, Long> {
    List<RemarqueDiagnostic> findByOrdreReparationIdOrderByCreatedAtDesc(Long ordreReparationId);
}
