package sn.oas.facturation.features.piecedetache.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.piecedetache.data.enums.TypePiece;

import java.util.List;

@Repository
public interface PieceDetacheRepository extends JpaRepository<PieceDetache, Long> {

    boolean existsByReference(String reference);

    List<PieceDetache> findByType(TypePiece type);
    Page<PieceDetache> findByType(TypePiece type, Pageable pageable);

    @Query("SELECT p FROM PieceDetache p WHERE " +
            "LOWER(p.reference) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.designation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.categorie.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<PieceDetache> searchPieces(@Param("keyword") String keyword);

    @Query("SELECT p FROM PieceDetache p WHERE " +
            "LOWER(p.reference) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.designation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.categorie.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<PieceDetache> searchPieces(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM (" +
            "SELECT 1 FROM lignes_ordre_reparation_piece WHERE piece_id = :id UNION ALL " +
            "SELECT 1 FROM lignes_facturation_piece WHERE piece_id = :id UNION ALL " +
            "SELECT 1 FROM ligne_bon_de_commande WHERE piece_detachee_id = :id UNION ALL " +
            "SELECT 1 FROM lignes_bon_de_sortie_piece WHERE piece_id = :id UNION ALL " +
            "SELECT 1 FROM piece_mouvements WHERE piece_id = :id" +
            ") AS usages", nativeQuery = true)
    boolean isPieceUsed(@Param("id") Long id);

    @Query(value = "SELECT DISTINCT id FROM (" +
            "SELECT piece_id as id FROM lignes_ordre_reparation_piece UNION " +
            "SELECT piece_id as id FROM lignes_facturation_piece UNION " +
            "SELECT piece_detachee_id as id FROM ligne_bon_de_commande UNION " +
            "SELECT piece_id as id FROM lignes_bon_de_sortie_piece UNION " +
            "SELECT piece_id as id FROM piece_mouvements" +
            ") AS all_usages WHERE id IS NOT NULL", nativeQuery = true)
    List<Long> getUsedPiecesIds();
}