package sn.oas.facturation.ficheAtelier.service;

import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.ficheAtelier.dto.FicheAtelierRequest;

import java.util.List;
import java.util.Optional;

import sn.oas.facturation.ficheAtelier.dto.FicheAtelierLightDTO;

public interface FicheAtelierService {
    FicheAtelier createFicheAtelier(FicheAtelierRequest request);
    List<FicheAtelierLightDTO> getAllFichesAtelier();
    Optional<FicheAtelier> getFicheAtelierById(Long id);
    FicheAtelier updateFicheAtelier(Long id, FicheAtelierRequest request);
    void deleteFicheAtelier(Long id);
    void assignMecanicien(Long ficheId, Long mecanicienId);
    void removeMecanicien(Long ficheId, Long mecanicienId);

    void assignMecanicienReparation(Long ficheId, Long mecanicienId);
    void removeMecanicienReparation(Long ficheId, Long mecanicienId);
    FicheAtelier updateStatut(Long id, String statut);
    boolean existsByVehiculeIdAndStatutNotIn(Long vehiculeId, List<sn.oas.facturation.ficheAtelier.data.enums.StatutFiche> statuts);
}
