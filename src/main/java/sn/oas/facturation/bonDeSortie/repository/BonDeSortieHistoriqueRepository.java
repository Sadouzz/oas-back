package sn.oas.facturation.bonDeSortie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.bonDeSortie.data.entity.BonDeSortieHistorique;

import java.util.List;

@Repository
public interface BonDeSortieHistoriqueRepository extends JpaRepository<BonDeSortieHistorique, Long> {

    List<BonDeSortieHistorique> findByBonDeSortieIdOrderByDateActionDesc(Long bonDeSortieId);

    List<BonDeSortieHistorique> findAllByOrderByDateActionDesc();
}
