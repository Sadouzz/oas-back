package sn.oas.facturation.bonDeCommande.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BonDeCommandeResponse {

    private Long id;

    private String numero;

    private LocalDateTime dateCommande;

    private String statut;

    private Long fournisseurId;

    private String fournisseurNom;

    private Long vehiculeId;

    private String immatriculationVehicule;

    private BigDecimal montantHT;

    private BigDecimal montantTVA;

    private BigDecimal montantTTC;

    private Boolean tvaApplicable;

    private Boolean paye;

    private String observation;

    private List<LigneBonDeCommandeResponse> lignes;
}