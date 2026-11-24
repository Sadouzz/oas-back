package sn.oas.facturation.features.facturation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.facturation.data.enums.TypeHistoriqueFacturation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacturationHistoriqueItemDto {
    private Long id;
    private TypeHistoriqueFacturation type;
    private String typeLabel;
    private String numero;

    private Long vehiculeId;
    private String immatriculation;
    private String vehiculeMarque;
    private String vehiculeModele;

    private Long clientId;
    private String clientNumero;
    private String clientNom;
    private String clientTelephone;

    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTimbre;
    private BigDecimal montantTTC;
    private BigDecimal montantTotal;

    private BigDecimal montantPaye;
    private BigDecimal resteAPayer;
    private String statut;

    private Long ordreReparationId;
    private String ordreReparationNumero;
    private String remarque;
}
