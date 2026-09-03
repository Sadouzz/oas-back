package sn.oas.facturation.features.categorie_pieces.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.categorie_pieces.data.entity.Categorie;
import sn.oas.facturation.features.categorie_pieces.dto.request.CategorieRequest;
import sn.oas.facturation.features.categorie_pieces.repository.CategorieRepository;
import sn.oas.facturation.features.depot_pieces.data.entity.Depot;
import sn.oas.facturation.features.depot_pieces.service.DepotService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategorieServiceImpl implements CategorieService {

    private final CategorieRepository categorieRepository;
    private final DepotService depotService;

    @Override
    public Categorie createCategorie(CategorieRequest request) {
        Long depotId = request.getEffectiveDepotId();
        Depot depot = null;
        if (depotId != null) {
            depot = depotService.getDepotById(depotId);
        }

        Categorie categorie = Categorie.builder()
                .nom(request.nom())
                .depot(depot)
                .build();

        return categorieRepository.save(categorie);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Categorie> getAllCategories(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return categorieRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categorie> getAllCategories() {
        return categorieRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Categorie getCategorieById(Long id) {
        return categorieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'id : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categorie> getCategoriesByDepotId(Long depotId) {
        return categorieRepository.findByDepotId(depotId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Categorie> getCategoriesByDepotId(Long depotId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return categorieRepository.findByDepotId(depotId, pageable);
    }

    @Override
    public Categorie updateCategorie(Long id, CategorieRequest request) {
        Categorie categorie = getCategorieById(id);
        categorie.setNom(request.nom());

        Long depotId = request.getEffectiveDepotId();
        if (depotId != null) {
            Depot depot = depotService.getDepotById(depotId);
            categorie.setDepot(depot);
        }

        return categorieRepository.save(categorie);
    }

    @Override
    public void deleteCategorie(Long id) {
        if (!categorieRepository.existsById(id)) {
            throw new RuntimeException("Catégorie non trouvée avec l'id : " + id);
        }
        categorieRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categorie> searchCategories(String keyword) {
        return categorieRepository.searchCategories(keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Categorie> searchCategories(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return categorieRepository.searchCategories(keyword, pageable);
    }
}
