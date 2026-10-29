package sn.oas.facturation.features.fournisseur.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.oas.facturation.features.fournisseur.data.entity.Fournisseur;
import sn.oas.facturation.features.fournisseur.dto.FournisseurRequest;
import sn.oas.facturation.features.fournisseur.repository.FournisseurRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FournisseurServiceImpl implements FournisseurService{
    private final FournisseurRepository fournisseurRepository;
    private final sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService documentNumberGeneratorService;

    @Override
    public Fournisseur createFournisseur(FournisseurRequest request) {
        String numero = request.getNumero();
        if (numero == null || numero.trim().isEmpty()) {
            numero = documentNumberGeneratorService.generateNextNumber(sn.oas.facturation.shared.documentNumber.DocumentType.FO);
        } else if (fournisseurRepository.existsByNumero(numero)) {
            throw new IllegalArgumentException("Numero déjà existant : " + numero);
        }
        Fournisseur fournisseur = Fournisseur.builder()
                .numero(numero)
                .nom(request.getNom())
                .archived(false)
                .build();

        return fournisseurRepository.save(fournisseur);
    }

    @Override
    public Page<Fournisseur> getAllFournisseur(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return fournisseurRepository.findAll(pageable);
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
        if (request.getNumero() != null) fournisseur.setNumero(request.getNumero());
        if (request.getNom() != null) fournisseur.setNom(request.getNom());

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

    @Override
    public Page<Fournisseur> searchFournisseur(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by("id").descending());
        return fournisseurRepository.searchFournisseurs(keyword, pageable);
    }
}
