package sn.oas.facturation.features.piecedetache.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.piecedetache.data.entity.PieceMouvement;
import sn.oas.facturation.features.piecedetache.data.enums.TypeMouvement;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PieceMouvementRepository extends JpaRepository<PieceMouvement, Long> {

    List<PieceMouvement> findByPieceOrderByDateOperationDesc(PDP piece);
    Page<PieceMouvement> findByPieceOrderByDateOperationDesc(PDP piece, Pageable pageable);

    List<PieceMouvement> findByPieceAndTypeOrderByDateOperationDesc(PDP piece, TypeMouvement type);
    Page<PieceMouvement> findByPieceAndTypeOrderByDateOperationDesc(PDP piece, TypeMouvement type, Pageable pageable);

    List<PieceMouvement> findByDateOperationBetweenOrderByDateOperationDesc(LocalDateTime debut, LocalDateTime fin);
    Page<PieceMouvement> findByDateOperationBetweenOrderByDateOperationDesc(LocalDateTime debut, LocalDateTime fin, Pageable pageable);

    List<PieceMouvement> findByPieceAndDateOperationBetweenOrderByDateOperationDesc(PDP piece, LocalDateTime debut, LocalDateTime fin);
    Page<PieceMouvement> findByPieceAndDateOperationBetweenOrderByDateOperationDesc(PDP piece, LocalDateTime debut, LocalDateTime fin, Pageable pageable);

    @Query("SELECT s FROM PieceMouvement s " +
           "LEFT JOIN s.piece p " +
           "LEFT JOIN p.categorie c " +
           "WHERE (:debut IS NULL OR s.dateOperation IS NULL OR s.dateOperation >= :debut) " +
           "AND (:fin IS NULL OR s.dateOperation IS NULL OR s.dateOperation <= :fin) " +
           "AND (:pieceId IS NULL OR p.id = :pieceId) " +
           "AND (:categorie IS NULL OR c.nom = :categorie) " +
           "AND (:type IS NULL OR s.type = :type) " +
           "ORDER BY s.dateOperation DESC, s.id DESC")
    List<PieceMouvement> findFiltered(
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin,
            @Param("pieceId") Long pieceId,
            @Param("categorie") String categorie,
            @Param("type") TypeMouvement type);

    @Query("SELECT s FROM PieceMouvement s " +
           "LEFT JOIN s.piece p " +
           "LEFT JOIN p.categorie c " +
           "WHERE (:debut IS NULL OR s.dateOperation IS NULL OR s.dateOperation >= :debut) " +
           "AND (:fin IS NULL OR s.dateOperation IS NULL OR s.dateOperation <= :fin) " +
           "AND (:pieceId IS NULL OR p.id = :pieceId) " +
           "AND (:categorie IS NULL OR c.nom = :categorie) " +
           "AND (:type IS NULL OR s.type = :type) " +
           "ORDER BY s.dateOperation DESC, s.id DESC")
    Page<PieceMouvement> findFiltered(
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin,
            @Param("pieceId") Long pieceId,
            @Param("categorie") String categorie,
            @Param("type") TypeMouvement type,
            Pageable pageable);

    @Query("SELECT s FROM PieceMouvement s " +
           "LEFT JOIN s.piece p " +
           "LEFT JOIN s.agent a " +
           "WHERE (s.numDocument IS NOT NULL AND LOWER(s.numDocument) LIKE :pattern) OR " +
           "(s.typeDocument IS NOT NULL AND LOWER(s.typeDocument) LIKE :pattern) OR " +
           "(s.numeroSerie IS NOT NULL AND LOWER(s.numeroSerie) LIKE :pattern) OR " +
           "(s.immatriculation IS NOT NULL AND LOWER(s.immatriculation) LIKE :pattern) OR " +
           "(s.prenom IS NOT NULL AND LOWER(s.prenom) LIKE :pattern) OR " +
           "(s.nom IS NOT NULL AND LOWER(s.nom) LIKE :pattern) OR " +
           "(s.motif IS NOT NULL AND LOWER(s.motif) LIKE :pattern) OR " +
           "(p.designation IS NOT NULL AND LOWER(p.designation) LIKE :pattern) OR " +
           "(p.reference IS NOT NULL AND LOWER(p.reference) LIKE :pattern) OR " +
           "(a.firstName IS NOT NULL AND LOWER(a.firstName) LIKE :pattern) OR " +
           "(a.lastName IS NOT NULL AND LOWER(a.lastName) LIKE :pattern) " +
           "ORDER BY s.dateOperation DESC, s.id DESC")
    List<PieceMouvement> searchMouvements(@Param("pattern") String pattern);

    @Query("SELECT s FROM PieceMouvement s " +
           "LEFT JOIN s.piece p " +
           "LEFT JOIN s.agent a " +
           "WHERE (s.numDocument IS NOT NULL AND LOWER(s.numDocument) LIKE :pattern) OR " +
           "(s.typeDocument IS NOT NULL AND LOWER(s.typeDocument) LIKE :pattern) OR " +
           "(s.numeroSerie IS NOT NULL AND LOWER(s.numeroSerie) LIKE :pattern) OR " +
           "(s.immatriculation IS NOT NULL AND LOWER(s.immatriculation) LIKE :pattern) OR " +
           "(s.prenom IS NOT NULL AND LOWER(s.prenom) LIKE :pattern) OR " +
           "(s.nom IS NOT NULL AND LOWER(s.nom) LIKE :pattern) OR " +
           "(s.motif IS NOT NULL AND LOWER(s.motif) LIKE :pattern) OR " +
           "(p.designation IS NOT NULL AND LOWER(p.designation) LIKE :pattern) OR " +
           "(p.reference IS NOT NULL AND LOWER(p.reference) LIKE :pattern) OR " +
           "(a.firstName IS NOT NULL AND LOWER(a.firstName) LIKE :pattern) OR " +
           "(a.lastName IS NOT NULL AND LOWER(a.lastName) LIKE :pattern) " +
           "ORDER BY s.dateOperation DESC, s.id DESC")
    Page<PieceMouvement> searchMouvements(@Param("pattern") String pattern, Pageable pageable);
}
