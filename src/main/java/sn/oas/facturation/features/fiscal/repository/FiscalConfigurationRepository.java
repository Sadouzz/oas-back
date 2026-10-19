package sn.oas.facturation.features.fiscal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.oas.facturation.features.fiscal.data.entity.FiscalConfiguration;

public interface FiscalConfigurationRepository extends JpaRepository<FiscalConfiguration, Long> {
}
