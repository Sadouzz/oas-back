package sn.oas.facturation.features.piecedetache.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import sn.oas.facturation.features.piecedetache.data.enums.TypePiece;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PieceDetacheRequest(
        TypePiece type,
        String reference,
        String designation,
        String categorie,
        Long depotId,
        String depot,
        Integer stockMagasin,
        Double prixUnitaire,
        Double prixGros,
        Double pourcentage,
        Double seuilMinimum
) {}