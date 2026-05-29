package sn.oas.facturation.ficheAtelier.service;

import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.ficheAtelier.dto.FicheAtelierRequest;

import java.util.List;
import java.util.Optional;

public interface FicheAtelierService {
    FicheAtelier createFicheAtelier(FicheAtelierRequest request);
    List<FicheAtelier> getAllFichesAtelier();
    Optional<FicheAtelier> getFicheAtelierById(Long id);
    FicheAtelier updateFicheAtelier(Long id, FicheAtelierRequest request);
    void deleteFicheAtelier(Long id);
}
