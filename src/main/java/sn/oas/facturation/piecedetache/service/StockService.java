package sn.oas.facturation.piecedetache.service;

import sn.oas.facturation.piecedetache.data.entity.StockMouvement;
import sn.oas.facturation.piecedetache.data.enums.TypeMouvement;
import sn.oas.facturation.piecedetache.dto.AjustementStockRequest;
import sn.oas.facturation.piecedetache.dto.EntreeStockRequest;
import sn.oas.facturation.piecedetache.dto.SortieStockRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface StockService {

    StockMouvement entree(EntreeStockRequest request);

    StockMouvement sortie(SortieStockRequest request);

    StockMouvement ajustement(AjustementStockRequest request);

    List<StockMouvement> getHistoriquePiece(Long pieceId);

    List<StockMouvement> getHistoriquePieceByType(Long pieceId, TypeMouvement type);

    List<StockMouvement> getHistoriqueGlobal(LocalDateTime debut, LocalDateTime fin, Long pieceId, String categorie, TypeMouvement type);
}
