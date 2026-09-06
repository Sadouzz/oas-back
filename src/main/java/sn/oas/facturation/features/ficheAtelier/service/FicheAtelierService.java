package sn.oas.facturation.features.ficheAtelier.service;

import org.springframework.data.domain.Page;

import sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.features.ficheAtelier.dto.FicheAtelierRequest;

import java.util.List;

public interface FicheAtelierService {
    FicheAtelier create(FicheAtelierRequest request);
    FicheAtelier update(Long id, FicheAtelierRequest request);
    FicheAtelier getById(Long id);
    FicheAtelier getByRendezVousId(Long rendezVousId);
    Page<FicheAtelier> getAll(int page, int size);
    List<FicheAtelier> getAll();
    void delete(Long id);
    FicheAtelier signForExit(Long id, String signature);
    boolean existsByOrdreReparationId(Long ordreReparationId);
}
