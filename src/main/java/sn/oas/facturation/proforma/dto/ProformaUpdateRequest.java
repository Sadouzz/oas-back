package sn.oas.facturation.proforma.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.Data;
import sn.oas.facturation.facturation.dto.LigneFacturationMainDoeuvreRequest;
import sn.oas.facturation.facturation.dto.LigneFacturationPieceRequest;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProformaUpdateRequest {
    private Long clientId;
    private Long vehiculeId;

    @Min(value = 0, message = "Le kilométrage doit être positif ou nul")
    private Double kilometrage;

    private String immatriculation;
    private String numeroChassis;
    private String marque;
    private String modele;

    @Min(value = 1900, message = "L'année doit être supérieure ou égale à 1900")
    private Integer annee;

    private String numeroBonDeCommande;
    private String remarque;
    private Double tvaRate;
    private BigDecimal montantTimbre;
    private BigDecimal montantAutre;

    @Valid
    private List<LigneFacturationPieceRequest> lignesPieces;

    @Valid
    private List<LigneFacturationMainDoeuvreRequest> lignesMainDoeuvres;
}
