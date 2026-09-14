package sn.oas.facturation.features.ordreReparation.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.ordreReparation.data.entity.FicheAtelierConfig;
@Repository
public interface FicheAtelierConfigRepository extends JpaRepository<FicheAtelierConfig, Long> {}
