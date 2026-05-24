package sn.oas.facturation.fournisseur.service;

import sn.oas.facturation.fournisseur.data.entity.Fournisseur;
import sn.oas.facturation.fournisseur.dto.FournisseurRequest;

import java.util.List;

public interface FournisseurService {
    Fournisseur createFournisseur(FournisseurRequest request);
    List<Fournisseur> getAllFournisseur();
    Fournisseur getFournisseurById(Long id);
    Fournisseur updateFournisseur(Long id, FournisseurRequest request);
    void archiveFournisseur(Long id);
    void unarchiveFournisseur(Long id);
    void deleteFournisseur(Long id);
    List<Fournisseur> searchFournisseur(String keyword);
}
