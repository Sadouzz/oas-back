package sn.oas.facturation.features.ficheAtelierConfig.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.ficheAtelierConfig.data.entity.FicheAtelierConfig;

import java.util.Optional;

@Repository
public interface FicheAtelierConfigRepository extends JpaRepository<FicheAtelierConfig, Long> {
    Optional<FicheAtelierConfig> findByGarageId(Long garageId);
    Optional<FicheAtelierConfig> findFirstByOrderByIdAsc();
}
