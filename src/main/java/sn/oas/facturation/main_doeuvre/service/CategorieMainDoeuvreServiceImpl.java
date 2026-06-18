package sn.oas.facturation.main_doeuvre.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.main_doeuvre.data.entity.CategorieMainDoeuvre;
import sn.oas.facturation.main_doeuvre.dto.CategorieMainDoeuvreRequest;
import sn.oas.facturation.main_doeuvre.repository.CategorieMainDoeuvreRepository;
import sn.oas.facturation.main_doeuvre.repository.MainDoeuvreRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategorieMainDoeuvreServiceImpl implements CategorieMainDoeuvreService {

    private final CategorieMainDoeuvreRepository categorieRepository;
    private final MainDoeuvreRepository mainDoeuvreRepository;

    @Override
    public List<CategorieMainDoeuvre> getAllCategories() {
        return categorieRepository.findAll();
    }

    @Override
    public CategorieMainDoeuvre getCategorieById(Long id) {
        return categorieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));
    }

    @Transactional
    @Override
    public CategorieMainDoeuvre createCategorie(CategorieMainDoeuvreRequest request) {
        CategorieMainDoeuvre categorie = CategorieMainDoeuvre.builder()
                .nom(request.nom())
                .build();
        return categorieRepository.save(categorie);
    }

    @Transactional
    @Override
    public CategorieMainDoeuvre updateCategorie(Long id, CategorieMainDoeuvreRequest request) {
        CategorieMainDoeuvre categorie = getCategorieById(id);
        categorie.setNom(request.nom());
        return categorieRepository.save(categorie);
    }

    @Transactional
    @Override
    public void deleteCategorie(Long id) {
        if (!categorieRepository.existsById(id)) {
            throw new RuntimeException("Catégorie non trouvée");
        }
        // Check if category is used
        long count = mainDoeuvreRepository.findAll().stream()
                .filter(m -> m.getCategorie() != null && m.getCategorie().getId().equals(id))
                .count();
        if (count > 0) {
            throw new RuntimeException("Impossible de supprimer la catégorie : elle est liée à " + count + " main(s) d'œuvre.");
        }
        categorieRepository.deleteById(id);
    }
}
