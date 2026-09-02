package sn.oas.facturation.features.piecedetache.service;

import org.springframework.data.domain.Page;
import sn.oas.facturation.features.piecedetache.data.entity.StockMouvement;
import sn.oas.facturation.features.piecedetache.data.enums.TypeMouvement;
import sn.oas.facturation.features.piecedetache.dto.AjustementStockRequest;
import sn.oas.facturation.features.piecedetache.dto.EntreeStockRequest;
import sn.oas.facturation.features.piecedetache.dto.SortieStockRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface StockService {

    StockMouvement entree(EntreeStockRequest request);

    StockMouvement sortie(SortieStockRequest request);

    StockMouvement ajustement(AjustementStockRequest request);

    List<StockMouvement> getHistoriquePiece(Long pieceId);
    Page<StockMouvement> getHistoriquePiece(Long pieceId, int page, int size);

    List<StockMouvement> getHistoriquePieceByType(Long pieceId, TypeMouvement type);
    Page<StockMouvement> getHistoriquePieceByType(Long pieceId, TypeMouvement type, int page, int size);

    List<StockMouvement> getHistoriqueGlobal(LocalDateTime debut, LocalDateTime fin, Long pieceId, String categorie, TypeMouvement type);
    Page<StockMouvement> getHistoriqueGlobal(LocalDateTime debut, LocalDateTime fin, Long pieceId, String categorie, TypeMouvement type, int page, int size);
}
