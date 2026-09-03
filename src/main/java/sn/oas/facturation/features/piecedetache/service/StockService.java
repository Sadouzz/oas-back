package sn.oas.facturation.features.piecedetache.service;

import org.springframework.data.domain.Page;
import sn.oas.facturation.features.piecedetache.data.entity.PieceMouvement;
import sn.oas.facturation.features.piecedetache.data.enums.TypeMouvement;
import sn.oas.facturation.features.piecedetache.dto.AjustementStockRequest;
import sn.oas.facturation.features.piecedetache.dto.EntreeStockRequest;
import sn.oas.facturation.features.piecedetache.dto.SortieStockRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface StockService {

    PieceMouvement entree(EntreeStockRequest request);

    PieceMouvement sortie(SortieStockRequest request);

    PieceMouvement ajustement(AjustementStockRequest request);

    List<PieceMouvement> getHistoriquePiece(Long pieceId);
    Page<PieceMouvement> getHistoriquePiece(Long pieceId, int page, int size);

    List<PieceMouvement> getHistoriquePieceByType(Long pieceId, TypeMouvement type);
    Page<PieceMouvement> getHistoriquePieceByType(Long pieceId, TypeMouvement type, int page, int size);

    List<PieceMouvement> getHistoriqueGlobal(LocalDateTime debut, LocalDateTime fin, Long pieceId, String categorie, TypeMouvement type);
    Page<PieceMouvement> getHistoriqueGlobal(LocalDateTime debut, LocalDateTime fin, Long pieceId, String categorie, TypeMouvement type, int page, int size);
    Page<PieceMouvement> getHistoriqueGlobal(String keyword, LocalDateTime debut, LocalDateTime fin, Long pieceId, String categorie, TypeMouvement type, int page, int size);
}
