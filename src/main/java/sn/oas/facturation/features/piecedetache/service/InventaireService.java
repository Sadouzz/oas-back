package sn.oas.facturation.features.piecedetache.service;

import sn.oas.facturation.features.piecedetache.dto.InventaireRequest;
import sn.oas.facturation.features.piecedetache.dto.InventaireResponse;

public interface InventaireService {

    InventaireResponse compterPiece(InventaireRequest request);
}