package sn.oas.facturation.features.vehicule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.vehicule.data.enums.TypeHistoriqueVehicule;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeHistoriqueItemDto {
    private Long id;
    private TypeHistoriqueVehicule type;
    private String typeLabel;
    private String numero;
    private LocalDateTime date;
    private String statut;
    private String description;
    private String meta;

    private BigDecimal montant;
    private BigDecimal montantPaye;
    private BigDecimal resteAPayer;

    private Long ordreReparationId;
    private String ordreReparationNumero;
    private Long agentId;
    private String agentNom;
    private Double kilometrage;
}
