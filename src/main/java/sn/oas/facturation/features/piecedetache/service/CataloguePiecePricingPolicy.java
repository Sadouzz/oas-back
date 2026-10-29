package sn.oas.facturation.features.piecedetache.service;

import org.springframework.stereotype.Component;
import sn.oas.facturation.features.piecedetache.data.entity.PDG;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;

/** Resolves catalogue-piece prices for new commercial document lines. */
@Component
public class CataloguePiecePricingPolicy {

    public double resolveForDocument(PieceDetache piece, Double linePrice) {
        if (piece == null) {
            throw new IllegalArgumentException("La pièce du catalogue est obligatoire.");
        }

        Double cataloguePrice = piece.getPrixUnitaire();
        if (piece instanceof PDP) {
            if (cataloguePrice == null) {
                throw new IllegalArgumentException("Le prix de cette PDP doit être renseigné dans le catalogue avant son utilisation.");
            }
            return cataloguePrice;
        }

        if (piece instanceof PDG) {
            if (cataloguePrice != null) {
                return cataloguePrice;
            }
            if (linePrice == null) {
                throw new IllegalArgumentException("Le prix de cette PDG est absent du catalogue. Saisissez un prix pour cette ligne.");
            }
            if (linePrice < 0) {
                throw new IllegalArgumentException("Le prix de la ligne PDG ne peut pas être négatif.");
            }
            return linePrice;
        }

        throw new IllegalArgumentException("Le type de pièce du catalogue n'est pas pris en charge.");
    }
}
