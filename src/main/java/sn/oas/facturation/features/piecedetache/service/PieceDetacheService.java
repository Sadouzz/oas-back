package sn.oas.facturation.features.piecedetache.service;

import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.piecedetache.data.enums.TypePiece;
import sn.oas.facturation.features.piecedetache.dto.PieceDetacheRequest;

import org.springframework.data.domain.Page;
import java.util.List;

public interface PieceDetacheService {

    Page<PieceDetache> getAllPieces(int page, int size);

    List<PieceDetache> getAllPieces();

    List<PieceDetache> filterByType(TypePiece type);
    Page<PieceDetache> filterByType(TypePiece type, int page, int size);

    List<PieceDetache> searchPieces(String keyword);
    Page<PieceDetache> searchPieces(String keyword, int page, int size);

    PieceDetache getById(Long id);

    PieceDetache create(PieceDetacheRequest request);

    PieceDetache update(Long id, PieceDetacheRequest request);

    void delete(Long id);

    PieceDetache restore(Long id);
}
