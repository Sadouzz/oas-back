package sn.oas.facturation.features.vehicule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeHistoriqueResponseDto {
    private Long vehiculeId;
    private String immatriculation;
    private String marque;
    private String modele;
    private Integer annee;
    private Double kilometrage;
    private String numeroChassis;
    private Boolean actif;
    private Long clientId;
    private String clientNom;
    private String clientTelephone;

    private long totalOperations;
    private long totalOrdresReparation;
    private long totalOrdresTravail;
    private long totalFactures;
    private long totalNotesDePrix;
    private long totalBonsSortie;
    private long totalBonsLivraison;
    private long totalDevis;
    private long totalRendezVous;

    private BigDecimal montantTotalFacture;
    private BigDecimal montantTotalPaye;
    private BigDecimal resteAPayer;

    private Integer page;
    private Integer size;
    private long totalElements;
    private int totalPages;

    private List<VehiculeHistoriqueItemDto> evenements;
}
