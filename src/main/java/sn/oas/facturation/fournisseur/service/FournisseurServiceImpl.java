package sn.oas.facturation.fournisseur.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.oas.facturation.fournisseur.data.entity.Fournisseur;
import sn.oas.facturation.fournisseur.dto.FournisseurRequest;
import sn.oas.facturation.fournisseur.repository.FournisseurRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FournisseurServiceImpl implements FournisseurService{
    private final FournisseurRepository fournisseurRepository;

    @Override
    public Fournisseur createFournisseur(FournisseurRequest request) {
        if (request.getMatricule() != null &&
                fournisseurRepository.existsByMatricule(request.getMatricule())) {
            throw new IllegalArgumentException(
                    "Matricule déjà existant : " + request.getMatricule()
            );
        }
        Fournisseur fournisseur = Fournisseur.builder()
                .matricule(request.getMatricule())
                .nomEntreprise(request.getNomEntreprise())
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .archived(false)
                .build();

        return fournisseurRepository.save(fournisseur);
    }

    @Override
    public List<Fournisseur> getAllFournisseur() {
        return fournisseurRepository.findAll();
    }

    @Override
    public Fournisseur getFournisseurById(Long id) {
        return fournisseurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));
    }

    @Override
    public Fournisseur updateFournisseur(Long id, FournisseurRequest request) {
        Fournisseur fournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));
        if (fournisseur.isArchived()) {
            throw new RuntimeException("Impossible de modifier un fournisseur archivé");
        }
        if (request.getMatricule() != null) fournisseur.setMatricule(request.getMatricule());
        if (request.getNomEntreprise() != null) fournisseur.setNomEntreprise(request.getNomEntreprise());
        if (request.getNom() != null) fournisseur.setNom(request.getNom());
        if (request.getPrenom() != null) fournisseur.setPrenom(request.getPrenom());

        return fournisseurRepository.save(fournisseur);
    }

    @Override
    public void archiveFournisseur(Long id) {
        Fournisseur fournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));
        if (fournisseur.isArchived()) {
            throw new RuntimeException("Fournisseur déjà archivé");
        }
        fournisseur.setArchived(true);
        fournisseurRepository.save(fournisseur);
    }

    @Override
    public void unarchiveFournisseur(Long id) {
        Fournisseur fournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));
        if (!fournisseur.isArchived()) {
            throw new RuntimeException("Fournisseur déjà actif");
        }
        fournisseur.setArchived(false);
        fournisseurRepository.save(fournisseur);
    }

    @Override
    public void deleteFournisseur(Long id) {
        if (!fournisseurRepository.existsById(id)) {
            throw new RuntimeException("Fournisseur non trouvé");
        }
        fournisseurRepository.deleteById(id);
    }

    @Override
    public List<Fournisseur> searchFournisseur(String keyword) {
        return fournisseurRepository.searchFournisseurs(keyword);
    }
}
