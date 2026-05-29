package sn.oas.facturation.ficheAtelier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;

@Repository
public interface FicheAtelierRepository extends JpaRepository<FicheAtelier, Long> {
}
