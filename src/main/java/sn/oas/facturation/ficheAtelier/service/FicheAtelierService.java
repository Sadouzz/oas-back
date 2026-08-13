package sn.oas.facturation.ficheAtelier.service;

import sn.oas.facturation.ficheAtelier.data.dto.FicheAtelierRequest;
import sn.oas.facturation.ficheAtelier.data.dto.FicheAtelierResponse;

import java.util.List;

public interface FicheAtelierService {
    FicheAtelierResponse create(FicheAtelierRequest request);
    FicheAtelierResponse update(Long id, FicheAtelierRequest request);
    FicheAtelierResponse getById(Long id);
    FicheAtelierResponse getByRendezVousId(Long rendezVousId);
    List<FicheAtelierResponse> getAll();
    void delete(Long id);
    FicheAtelierResponse signForExit(Long id, String signature);
}
