package sn.oas.facturation.features.factureAchetee.service;

import org.springframework.data.domain.Page;
import sn.oas.facturation.features.factureAchetee.data.entity.FactureAchetee;
import sn.oas.facturation.features.factureAchetee.dto.FactureAcheteeRequest;

import java.time.YearMonth;

public interface FactureAcheteeService {

    FactureAchetee create(FactureAcheteeRequest request);

    Page<FactureAchetee> getByMonth(YearMonth month, int page, int size);

    void delete(Long id);
}
