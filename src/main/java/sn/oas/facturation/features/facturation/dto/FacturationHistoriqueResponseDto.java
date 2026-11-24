package sn.oas.facturation.features.facturation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacturationHistoriqueResponseDto {
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Long clientId;
    private String clientNumero;

    private long totalOperations;
    private long totalBonsLivraison;
    private long totalFactures;
    private long totalAvoirsTTC;
    private long totalNotesDePrix;
    private long totalAvoirsHT;

    private BigDecimal montantTotalBonsLivraisonTTC;
    private BigDecimal montantTotalFacturesTTC;
    private BigDecimal montantTotalAvoirsTTC;
    private BigDecimal montantTotalNotesDePrixHT;
    private BigDecimal montantTotalAvoirsHT;

    private Integer page;
    private Integer size;
    private long totalElements;
    private int totalPages;

    private List<FacturationHistoriqueItemDto> evenements;
}
