package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.Data;
import sn.oas.facturation.features.ordreReparation.dto.responses.AdditionalStubs.*;
import java.util.List;

@Data
public class ProformaSummaryDto {
    private Long id;
    private String statut;
    private Double montantHT;
    private Double montantTVA;
    private Double montantTTC;
    private String numero;
    private java.time.LocalDateTime dateCreation;
    private List<LigneProformaPieceDto> lignesPieces;
    private List<LigneProformaMoDto> lignesMainDoeuvres;
}
