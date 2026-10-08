package sn.oas.facturation.features.vehiculeTransfer.data.dto;
import jakarta.validation.constraints.Size;
public record VehicleTransferDecision(boolean approved, @Size(max = 2000) String decisionNote) {}
