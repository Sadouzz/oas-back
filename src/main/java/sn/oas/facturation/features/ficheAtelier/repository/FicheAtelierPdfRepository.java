package sn.oas.facturation.features.ficheAtelier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelierPdf;

public interface FicheAtelierPdfRepository extends JpaRepository<FicheAtelierPdf, Long> {
}
