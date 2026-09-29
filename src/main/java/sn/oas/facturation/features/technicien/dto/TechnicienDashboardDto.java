package sn.oas.facturation.features.technicien.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TechnicienDashboardDto {
    private long totalDiagnostics;
    private long totalReparations;
    private long totalTermines;
}
