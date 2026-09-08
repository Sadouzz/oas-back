package sn.oas.facturation.features.piecedetache.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.piecedetache.data.entity.PieceMouvement;
import sn.oas.facturation.features.piecedetache.data.enums.TypeMouvement;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PieceMouvementRepository extends JpaRepository<PieceMouvement, Long>, JpaSpecificationExecutor<PieceMouvement> {

    List<PieceMouvement> findByPieceOrderByDateOperationDesc(PDP piece);
    Page<PieceMouvement> findByPieceOrderByDateOperationDesc(PDP piece, Pageable pageable);

    List<PieceMouvement> findByPieceAndTypeOrderByDateOperationDesc(PDP piece, TypeMouvement type);
    Page<PieceMouvement> findByPieceAndTypeOrderByDateOperationDesc(PDP piece, TypeMouvement type, Pageable pageable);

    List<PieceMouvement> findByDateOperationBetweenOrderByDateOperationDesc(LocalDateTime debut, LocalDateTime fin);
    Page<PieceMouvement> findByDateOperationBetweenOrderByDateOperationDesc(LocalDateTime debut, LocalDateTime fin, Pageable pageable);

    List<PieceMouvement> findByPieceAndDateOperationBetweenOrderByDateOperationDesc(PDP piece, LocalDateTime debut, LocalDateTime fin);
    Page<PieceMouvement> findByPieceAndDateOperationBetweenOrderByDateOperationDesc(PDP piece, LocalDateTime debut, LocalDateTime fin, Pageable pageable);
}
