package sn.oas.facturation.features.bonDeSortie.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;

public record BonDeSortieRequest(
        Long clientId,
        Long vehiculeId,
        @JsonAlias({"lignesBonDeSortiePieces", "pieces", "lignesPiece"})
        List<LignePieceRequest> lignesPieces,
        @JsonAlias({"lignesBonDeSortieMainDoeuvres", "lignesMainDoeuvre", "mainDoeuvres", "lignesMO"})
        List<LigneMainDoeuvreRequest> lignesMainDoeuvres,
        Long ordreReparationId,
        String remarque,
        Long ordreTravailId
) {
    public BonDeSortieRequest(Long clientId, Long vehiculeId, List<LignePieceRequest> lignesPieces, Long ordreReparationId, String remarque, Long ordreTravailId) {
        this(clientId, vehiculeId, lignesPieces, null, ordreReparationId, remarque, ordreTravailId);
    }
}
