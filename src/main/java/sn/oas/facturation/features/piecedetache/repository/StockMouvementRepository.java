package sn.oas.facturation.features.piecedetache.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.piecedetache.data.entity.StockMouvement;
import sn.oas.facturation.features.piecedetache.data.enums.TypeMouvement;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMouvementRepository extends JpaRepository<StockMouvement, Long> {

    List<StockMouvement> findByPieceOrderByDateOperationDesc(PDP piece);

    List<StockMouvement> findByPieceAndTypeOrderByDateOperationDesc(PDP piece, TypeMouvement type);

    List<StockMouvement> findByDateOperationBetweenOrderByDateOperationDesc(LocalDateTime debut, LocalDateTime fin);

    List<StockMouvement> findByPieceAndDateOperationBetweenOrderByDateOperationDesc(PDP piece, LocalDateTime debut, LocalDateTime fin);

    @Query("SELECT s FROM StockMouvement s WHERE s.dateOperation BETWEEN :debut AND :fin " +
           "AND (:pieceId IS NULL OR s.piece.id = :pieceId) " +
           "AND (:categorie IS NULL OR s.piece.categorie = :categorie) " +
           "AND (:type IS NULL OR s.type = :type) " +
           "ORDER BY s.dateOperation DESC")
    List<StockMouvement> findFiltered(
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin,
            @Param("pieceId") Long pieceId,
            @Param("categorie") String categorie,
            @Param("type") TypeMouvement type);

    @Query("SELECT s FROM StockMouvement s WHERE LOWER(s.motif) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<StockMouvement> searchMouvements(@Param("keyword") String keyword);
}
