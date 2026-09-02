package sn.oas.facturation.features.fournisseur.service;

import sn.oas.facturation.features.fournisseur.data.entity.Fournisseur;
import sn.oas.facturation.features.fournisseur.dto.FournisseurRequest;

import org.springframework.data.domain.Page;
import java.util.List;

public interface FournisseurService {
    Fournisseur createFournisseur(FournisseurRequest request);
    Page<Fournisseur> getAllFournisseur(int page, int size);
    List<Fournisseur> getAllFournisseur();
    Fournisseur getFournisseurById(Long id);
    Fournisseur updateFournisseur(Long id, FournisseurRequest request);
    void archiveFournisseur(Long id);
    void unarchiveFournisseur(Long id);
    void deleteFournisseur(Long id);
    List<Fournisseur> searchFournisseur(String keyword);
    Page<Fournisseur> searchFournisseur(String keyword, int page, int size);
}
