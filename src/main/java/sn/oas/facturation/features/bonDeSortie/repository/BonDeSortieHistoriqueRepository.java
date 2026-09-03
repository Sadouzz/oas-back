package sn.oas.facturation.features.bonDeSortie.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortieHistorique;

import java.util.List;

@Repository
public interface BonDeSortieHistoriqueRepository extends JpaRepository<BonDeSortieHistorique, Long> {

    List<BonDeSortieHistorique> findByBonDeSortieIdOrderByDateActionDesc(Long bonDeSortieId);
    Page<BonDeSortieHistorique> findByBonDeSortieId(Long bonDeSortieId, Pageable pageable);

    List<BonDeSortieHistorique> findAllByOrderByDateActionDesc();
    Page<BonDeSortieHistorique> findAll(Pageable pageable);

    @Query("SELECT h FROM BonDeSortieHistorique h WHERE " +
            "(h.numBs IS NOT NULL AND LOWER(h.numBs) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(h.numeroSerie IS NOT NULL AND LOWER(h.numeroSerie) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(h.immatriculation IS NOT NULL AND LOWER(h.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(h.designation IS NOT NULL AND LOWER(h.designation) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(h.statut IS NOT NULL AND LOWER(h.statut) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(h.prenom IS NOT NULL AND LOWER(h.prenom) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(h.nom IS NOT NULL AND LOWER(h.nom) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<BonDeSortieHistorique> searchHistorique(@Param("keyword") String keyword);

    @Query("SELECT h FROM BonDeSortieHistorique h WHERE " +
            "(h.numBs IS NOT NULL AND LOWER(h.numBs) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(h.numeroSerie IS NOT NULL AND LOWER(h.numeroSerie) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(h.immatriculation IS NOT NULL AND LOWER(h.immatriculation) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(h.designation IS NOT NULL AND LOWER(h.designation) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(h.statut IS NOT NULL AND LOWER(h.statut) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(h.prenom IS NOT NULL AND LOWER(h.prenom) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(h.nom IS NOT NULL AND LOWER(h.nom) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<BonDeSortieHistorique> searchHistorique(@Param("keyword") String keyword, Pageable pageable);
}
