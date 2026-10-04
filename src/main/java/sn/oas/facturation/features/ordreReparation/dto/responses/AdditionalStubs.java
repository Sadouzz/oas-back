package sn.oas.facturation.features.ordreReparation.dto.responses;

import lombok.Data;
import java.util.List;

@Data
public class AdditionalStubs {
    @Data public static class LigneTravailOrdreDto { private Long id; private String description; }
    @Data public static class LigneReceptionOrdreDto { private Long id; private String description; }
    @Data public static class TechnicienDto { private Long id; private String nom; private String prenom; }
    @Data public static class RemarqueDiagnosticDto { private Long id; private String texte; }
    @Data public static class PieceJointeDiagnosticDto { private Long id; private String url; private String type; }
    @Data public static class LigneProformaPieceDto { private Long id; private String nom; private Double prix; private Integer quantite; private Double montantTotal; private Boolean isCustom; private String type; }
    @Data public static class LigneProformaMoDto { private Long id; private String nom; private Double tarifHoraire; private Integer nbreHeure; private Double montantTotal;  }
    @Data public static class BonCommandeSummaryDto { private Long id; private String reference; }
}
