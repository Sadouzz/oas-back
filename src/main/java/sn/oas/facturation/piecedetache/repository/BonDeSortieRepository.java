package sn.oas.facturation.piecedetache.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.piecedetache.data.entity.BonDeSortie;
import sn.oas.facturation.piecedetache.data.enums.StatutBon;

import java.util.List;
import java.util.Optional;

@Repository
public interface BonDeSortieRepository extends JpaRepository<BonDeSortie, Long> {

    Optional<BonDeSortie> findByReference(String reference);

    List<BonDeSortie> findByStatutOrderByDateDesc(StatutBon statut);

    List<BonDeSortie> findByClientIdOrderByDateDesc(Long clientId);

    List<BonDeSortie> findByVehiculeIdOrderByDateDesc(Long vehiculeId);

    @Query("SELECT COUNT(b) FROM BonDeSortie b WHERE YEAR(b.date) = :year")
    long countByAnnee(@Param("year") int year);
}