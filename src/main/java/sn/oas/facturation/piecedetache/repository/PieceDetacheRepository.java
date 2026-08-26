package sn.oas.facturation.piecedetache.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.piecedetache.data.enums.StatutPiece;
import sn.oas.facturation.piecedetache.data.enums.TypePiece;

import java.util.List;

@Repository
public interface PieceDetacheRepository extends JpaRepository<PieceDetache, Long> {

    boolean existsByReference(String reference);

    List<PieceDetache> findByType(TypePiece type);

    @Query("SELECT p FROM PieceDetache p WHERE " +
            "LOWER(p.reference) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.designation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.categorie) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<PieceDetache> searchPieces(@Param("keyword") String keyword);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM (" +
            "SELECT 1 FROM lignes_ordre_reparation_piece WHERE piece_id = :id UNION ALL " +
            "SELECT 1 FROM lignes_facturation_piece WHERE piece_id = :id UNION ALL " +
            "SELECT 1 FROM ligne_bon_de_commande WHERE piece_detachee_id = :id UNION ALL " +
            "SELECT 1 FROM lignes_bon_de_sortie_piece WHERE piece_id = :id UNION ALL " +
            "SELECT 1 FROM stock_garage WHERE piece_id = :id UNION ALL " +
            "SELECT 1 FROM stock_mouvements WHERE piece_id = :id" +
            ") AS usages", nativeQuery = true)
    boolean isPieceUsed(@Param("id") Long id);

    @Query(value = "SELECT DISTINCT id FROM (" +
            "SELECT piece_id as id FROM lignes_ordre_reparation_piece UNION " +
            "SELECT piece_id as id FROM lignes_facturation_piece UNION " +
            "SELECT piece_detachee_id as id FROM ligne_bon_de_commande UNION " +
            "SELECT piece_id as id FROM lignes_bon_de_sortie_piece UNION " +
            "SELECT piece_id as id FROM stock_garage UNION " +
            "SELECT piece_id as id FROM stock_mouvements" +
            ") AS all_usages WHERE id IS NOT NULL", nativeQuery = true)
    List<Long> getUsedPiecesIds();
}