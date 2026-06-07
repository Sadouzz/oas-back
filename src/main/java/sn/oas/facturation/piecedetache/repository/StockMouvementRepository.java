package sn.oas.facturation.piecedetache.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.piecedetache.data.entity.PDP;
import sn.oas.facturation.piecedetache.data.entity.StockMouvement;
import sn.oas.facturation.piecedetache.data.enums.TypeMouvement;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMouvementRepository extends JpaRepository<StockMouvement, Long> {

    List<StockMouvement> findByPieceOrderByDateOperationDesc(PDP piece);

    List<StockMouvement> findByPieceAndTypeOrderByDateOperationDesc(PDP piece, TypeMouvement type);

    List<StockMouvement> findByDateOperationBetweenOrderByDateOperationDesc(LocalDateTime debut, LocalDateTime fin);

    List<StockMouvement> findByPieceAndDateOperationBetweenOrderByDateOperationDesc(PDP piece, LocalDateTime debut, LocalDateTime fin);

    @Query("SELECT s FROM StockMouvement s WHERE LOWER(s.motif) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<StockMouvement> searchMouvements(@Param("keyword") String keyword);
}
