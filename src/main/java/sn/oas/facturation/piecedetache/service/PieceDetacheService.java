package sn.oas.facturation.piecedetache.service;

import sn.oas.facturation.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.piecedetache.data.enums.StatutPiece;
import sn.oas.facturation.piecedetache.data.enums.TypePiece;
import sn.oas.facturation.piecedetache.dto.PieceDetacheRequest;

import java.util.List;

public interface PieceDetacheService {

    List<PieceDetache> getAllPieces();

    List<PieceDetache> filterByType(TypePiece type);

    List<PieceDetache> searchPieces(String keyword);

    PieceDetache getById(Long id);

    PieceDetache create(PieceDetacheRequest request);

    PieceDetache update(Long id, PieceDetacheRequest request);

    void delete(Long id);

    PieceDetache restore(Long id);
}
