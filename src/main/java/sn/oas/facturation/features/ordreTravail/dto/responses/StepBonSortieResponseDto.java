package sn.oas.facturation.features.ordreTravail.dto.responses;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sn.oas.facturation.features.ordreReparation.dto.responses.BonDeSortieSummaryDto;
import sn.oas.facturation.features.proforma.dto.ProformaDetailsResponseDto.LignePieceResponseDto;
import sn.oas.facturation.features.proforma.dto.ProformaDetailsResponseDto.LigneMainDoeuvreResponseDto;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StepBonSortieResponseDto extends BaseOtStepResponseDto {
    private Long clientId;
    private String ordreReparationStatut;
    private Boolean hasBonDeSortie = false;
    private BonDeSortieSummaryDto bonDeSortie;
    private Integer quantiteTotalePieces = 0;
    private Double montantTotalBons = 0.0;
    private String proformaNumero;

    // --- Pièces et MO du PROFORMA ---
    @JsonProperty("lignesPiecesProforma")
    @JsonAlias({"piecesProforma", "piecesManquantesProforma"})
    private List<LignePieceResponseDto> lignesPiecesProforma = new ArrayList<>();

    @JsonProperty("lignesMainDoeuvresProforma")
    @JsonAlias({"mainDoeuvresProforma"})
    private List<LigneMainDoeuvreResponseDto> lignesMainDoeuvresProforma = new ArrayList<>();

    // --- Pièces et MO du BON DE SORTIE (BS) ---
    @JsonProperty("lignesPiecesBS")
    @JsonAlias({"lignesPiecesBonDeSortie", "piecesBS", "piecesBonDeSortie"})
    private List<LignePieceResponseDto> lignesPiecesBS = new ArrayList<>();

    @JsonProperty("lignesMainDoeuvresBS")
    @JsonAlias({"lignesMainDoeuvresBonDeSortie", "mainDoeuvresBS", "mainDoeuvresBonDeSortie"})
    private List<LigneMainDoeuvreResponseDto> lignesMainDoeuvresBS = new ArrayList<>();

    // --- Rétro-compatibilité pour lignesPieces et lignesMainDoeuvres ---
    @JsonProperty("lignesPieces")
    private List<LignePieceResponseDto> lignesPieces = new ArrayList<>();

    @JsonProperty("lignesMainDoeuvres")
    private List<LigneMainDoeuvreResponseDto> lignesMainDoeuvres = new ArrayList<>();
}
