package sn.oas.facturation.features.vehiculeTransfer.data.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record VehicleTransferRequestCreate(@NotBlank @Size(max = 30) String immatriculation,
        @Size(max = 100) String numeroChassis, @Size(max = 2000) String requestNote) {}
