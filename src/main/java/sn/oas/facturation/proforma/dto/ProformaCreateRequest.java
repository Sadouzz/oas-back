package sn.oas.facturation.proforma.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import sn.oas.facturation.facturation.dto.LigneFacturationMainDoeuvreRequest;
import sn.oas.facturation.facturation.dto.LigneFacturationPieceRequest;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProformaCreateRequest {
    @NotNull(message = "Le client est obligatoire")
    private Long clientId;

    private Long vehiculeId;

    private Long ordreReparationId;

    @NotNull(message = "Le kilométrage est obligatoire")
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

    private Double tvaRate; // taux TVA en pourcentage (ex: 18.0)

    private BigDecimal montantTimbre; // montant timbre fiscal

    private BigDecimal montantAutre; // autre montant (ex: frais additionnels)

    @Valid
    private List<LigneFacturationPieceRequest> lignesPieces;

    @Valid
    private List<LigneFacturationMainDoeuvreRequest> lignesMainDoeuvres;
}
