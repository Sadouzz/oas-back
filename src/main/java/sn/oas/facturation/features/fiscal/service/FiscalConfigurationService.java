package sn.oas.facturation.features.fiscal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.fiscal.data.entity.FiscalConfiguration;
import sn.oas.facturation.features.fiscal.repository.FiscalConfigurationRepository;
import sn.oas.facturation.features.fiscal.dto.FiscalConfigurationRequest;
import sn.oas.facturation.shared.exception.BadRequestException;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class FiscalConfigurationService {
    private final FiscalConfigurationRepository repository;

    @Transactional
    public FiscalConfiguration getCurrent() {
        return repository.findById(1L).orElseGet(() -> repository.save(new FiscalConfiguration()));
    }

    @Transactional
    public FiscalConfiguration update(FiscalConfigurationRequest request) {
        validateRate(request.tvaRate(), "TVA");
        validateAmount(request.timbreFiscal(), "Timbre fiscal");
        FiscalConfiguration current = getCurrent();
        current.setTvaRate(request.tvaRate().setScale(4, RoundingMode.HALF_UP));
        current.setTimbreFiscal(request.timbreFiscal().setScale(2, RoundingMode.HALF_UP));
        return repository.save(current);
    }

    public BigDecimal calculateRate(BigDecimal base, BigDecimal ratePercent) {
        return base.multiply(ratePercent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    private void validateRate(BigDecimal value, String label) {
        if (value == null || value.signum() < 0 || value.compareTo(new BigDecimal("100")) > 0)
            throw new BadRequestException(label + " doit être compris entre 0 et 100 %.");
    }

    private void validateAmount(BigDecimal value, String label) {
        if (value == null || value.signum() < 0 || value.compareTo(new BigDecimal("100000000")) > 0)
            throw new BadRequestException(label + " doit être positif et inférieur ou égal à 100 000 000.");
    }
}
