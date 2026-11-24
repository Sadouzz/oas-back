package sn.oas.facturation.features.tva.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.avoirTvaAchetee.dto.AvoirTvaAcheteeResponse;
import sn.oas.facturation.features.factureAchetee.dto.FactureAcheteeResponse;
import sn.oas.facturation.shared.dto.PageResponse;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TvaDeclarationResponseDto {
    private String month;
    private String previousMonth;

    private PageResponse<TvaVenteItemDto> facturesVendues;
    private PageResponse<TvaVenteItemDto> avoirsVendus;
    private PageResponse<FactureAcheteeResponse> facturesAchetees;
    private PageResponse<AvoirTvaAcheteeResponse> avoirsTvaAchetees;

    private BigDecimal totalTvaVendue;
    private BigDecimal totalTimbreVendu;
    private BigDecimal totalTvaAvoirVendu;
    private BigDecimal totalTimbreAvoirVendu;

    /** TVA collectée nette (Total tva à l'écran / Rapport TVA vendue sur le PDF). */
    private BigDecimal rapportTvaVendue;
    private BigDecimal totalTimbre;

    private BigDecimal totalTvaAchetee;
    private BigDecimal totalAvoirTvaAchetee;

    /** TVA déductible nette (Rapport TVA achetée sur le PDF). */
    private BigDecimal rapportTvaAchetee;

    /** Crédit de TVA reporté depuis le mois précédent (<= 0, ou 0). */
    private BigDecimal rapport;

    /** Différence du mois = rapportTvaVendue - rapportTvaAchetee. */
    private BigDecimal difference;

    /** Solde final = difference + rapport. */
    private BigDecimal solde;
}
