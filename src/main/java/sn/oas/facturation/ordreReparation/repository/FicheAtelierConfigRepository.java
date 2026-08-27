package sn.oas.facturation.ordreReparation.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.ordreReparation.data.entity.FicheAtelierConfig;
@Repository
public interface FicheAtelierConfigRepository extends JpaRepository<FicheAtelierConfig, Long> {}
