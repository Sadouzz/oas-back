package sn.oas.facturation.features.clientportal.dto;

import java.time.LocalDateTime;
import java.util.List;

public record FicheEnCoursSummaryDTO(
        Long id,
        String numero,
        String statut,
        String stageLabel,
        String stageTone,
        LocalDateTime dateCreation,
        LocalDateTime updatedAt,
        LocalDateTime dateSortie,
        String listeDefauts,
        String descriptionTravaux,
        List<LigneReceptionItemDTO> lignesReception,
        List<LignePieceSummaryDTO> lignesOrdreReparationPieces,
        List<LigneMOSummaryDTO> lignesOrdreReparationMainDoeuvres
) {
    public record LignePieceSummaryDTO(
            Long id,
            Integer quantite,
            Integer prix,
            PieceRefDTO piece
    ) {
        public record PieceRefDTO(String reference) {}
    }

    public record LigneMOSummaryDTO(
            Long id,
            Integer nbreHeure,
            Integer prix,
            MORefDTO mainDoeuvre
    ) {
        public record MORefDTO(String description) {}
    }
}
