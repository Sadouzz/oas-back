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

    boolean existsByNumeroDeSerie(String numeroDeSerie);

    List<PieceDetache> findByStatut(StatutPiece statut);

    List<PieceDetache> findByType(TypePiece type);

    @Query("SELECT p FROM PieceDetache p WHERE " +
            "LOWER(p.numeroDeSerie) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.categorie) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<PieceDetache> search(@Param("keyword") String keyword);
}
