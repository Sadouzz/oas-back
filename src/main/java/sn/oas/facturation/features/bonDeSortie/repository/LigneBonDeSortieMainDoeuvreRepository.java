package sn.oas.facturation.features.bonDeSortie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.bonDeSortie.data.entity.LigneBonDeSortieMainDoeuvre;

import java.util.List;

@Repository
public interface LigneBonDeSortieMainDoeuvreRepository extends JpaRepository<LigneBonDeSortieMainDoeuvre, Long> {
    List<LigneBonDeSortieMainDoeuvre> findByBonDeSortieId(Long bonDeSortieId);
}
