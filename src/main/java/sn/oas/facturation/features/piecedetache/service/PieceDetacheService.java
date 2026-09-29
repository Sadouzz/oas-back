package sn.oas.facturation.features.piecedetache.service;

import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.piecedetache.data.enums.TypePiece;
import sn.oas.facturation.features.piecedetache.dto.PieceDetacheRequest;
import sn.oas.facturation.features.piecedetache.dto.PieceStatsResponse;

import sn.oas.facturation.features.piecedetache.data.enums.StatutPiece;

import org.springframework.data.domain.Page;
import java.util.List;

public interface PieceDetacheService {

    Page<PieceDetache> getAllPieces(int page, int size);

    List<PieceDetache> getAllPieces();

    List<PieceDetache> filterByType(TypePiece type);
    Page<PieceDetache> filterByType(TypePiece type, int page, int size);

    List<PieceDetache> filterByDepot(Long depotId);
    Page<PieceDetache> filterByDepot(Long depotId, int page, int size);

    Page<PieceDetache> getPieces(TypePiece type, Long depotId, String keyword, int page, int size);
    List<PieceDetache> getPieces(TypePiece type, Long depotId, String keyword);

    Page<PieceDetache> getPieces(TypePiece type, StatutPiece statut, Long depotId, String depotNom, String keyword, int page, int size);
    List<PieceDetache> getPieces(TypePiece type, StatutPiece statut, Long depotId, String depotNom, String keyword);

    List<PieceDetache> searchPieces(String keyword);
    Page<PieceDetache> searchPieces(String keyword, int page, int size);

    PieceDetache getById(Long id);

    PieceDetache create(PieceDetacheRequest request);

    PieceDetache update(Long id, PieceDetacheRequest request);

    void delete(Long id);

    PieceDetache restore(Long id);

    PieceStatsResponse getStats();
}
