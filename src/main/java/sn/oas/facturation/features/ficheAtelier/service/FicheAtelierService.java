package sn.oas.facturation.features.ficheAtelier.service;

import sn.oas.facturation.features.ficheAtelier.data.dto.FicheAtelierRequest;
import sn.oas.facturation.features.ficheAtelier.data.dto.FicheAtelierResponse;

import org.springframework.data.domain.Page;
import java.util.List;

public interface FicheAtelierService {
    FicheAtelierResponse create(FicheAtelierRequest request);
    FicheAtelierResponse update(Long id, FicheAtelierRequest request);
    FicheAtelierResponse getById(Long id);
    FicheAtelierResponse getByRendezVousId(Long rendezVousId);
    Page<FicheAtelierResponse> getAll(int page, int size);
    List<FicheAtelierResponse> getAll();
    void delete(Long id);
    FicheAtelierResponse signForExit(Long id, String signature);
}
