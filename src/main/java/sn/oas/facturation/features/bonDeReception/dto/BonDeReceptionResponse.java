package sn.oas.facturation.features.bonDeReception.dto;

import lombok.Builder;
import lombok.Data;
import sn.oas.facturation.features.facturation.dto.LigneFacturationMainDoeuvreResponse;
import sn.oas.facturation.features.facturation.dto.LigneFacturationPieceResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BonDeReceptionResponse {
    private Long id;
    private String numero;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;
    private BigDecimal montantTimbre;
    private BigDecimal montantTotal;
    private Long agentId;
    private String agentNom;
    private String remarque;
    private Double kilometrage;
    
    private Long bonDeCommandeId;
    private String bonDeCommandeNumero;
    
    private List<LigneFacturationPieceResponse> lignesPieces;
    private List<LigneFacturationMainDoeuvreResponse> lignesMainDoeuvres;
}
