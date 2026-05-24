package sn.oas.facturation.piecedetache.service;

import sn.oas.facturation.piecedetache.dto.InventaireRequest;
import sn.oas.facturation.piecedetache.dto.InventaireResponse;

public interface InventaireService {

    InventaireResponse compterPiece(InventaireRequest request);
}