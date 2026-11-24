package sn.oas.facturation.features.avoirTvaAchetee.service;

import org.springframework.data.domain.Page;
import sn.oas.facturation.features.avoirTvaAchetee.data.entity.AvoirTvaAchetee;
import sn.oas.facturation.features.avoirTvaAchetee.dto.AvoirTvaAcheteeRequest;

import java.time.YearMonth;

public interface AvoirTvaAcheteeService {

    AvoirTvaAchetee create(AvoirTvaAcheteeRequest request);

    Page<AvoirTvaAchetee> getByMonth(YearMonth month, int page, int size);

    void delete(Long id);
}
