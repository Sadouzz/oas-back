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

import sn.oas.facturation.features.depot_pieces.repository.DepotRepository;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategorieServiceImpl implements CategorieService {

    private final CategorieRepository categorieRepository;
    private final DepotService depotService;
    private final DepotRepository depotRepository;
    private final DocumentNumberGeneratorService documentNumberGeneratorService;

    private Depot getOrCreatePdgDepot() {
        Garage currentGarage = documentNumberGeneratorService.getCurrentGarage();
        Long garageId = currentGarage != null ? currentGarage.getId() : null;
        if (garageId != null) {
            return depotRepository.findFirstByGarageIdAndNomIgnoreCase(garageId, "PDG")
                    .or(() -> depotRepository.findFirstByNomIgnoreCase("PDG"))
                    .orElseGet(() -> {
                        Depot newDepot = Depot.builder()
                                .nom("PDG")
                                .description("Dépôt automatique pour pièces générées (PDG)")
                                .garage(currentGarage)
                                .build();
                        return depotRepository.save(newDepot);
                    });
        }
        return depotRepository.findFirstByNomIgnoreCase("PDG")
                .orElseGet(() -> {
                    Depot newDepot = Depot.builder()
                            .nom("PDG")
                            .description("Dépôt automatique pour pièces générées (PDG)")
                            .garage(currentGarage)
                            .build();
                    return depotRepository.save(newDepot);
                });
    }

    @Override
    public Categorie createCategorie(CategorieRequest request) {
        List<Long> depotIds = request.getEffectiveDepotIds();
        List<Depot> depots = new ArrayList<>();
        if (depotIds != null && !depotIds.isEmpty()) {
            for (Long dId : depotIds) {
                try {
                    Depot d = depotService.getDepotById(dId);
                    if (d != null && !depots.contains(d)) depots.add(d);
                } catch (Exception ignored) {}
            }
        }

        // Attachement automatique du dépôt PDG à toute catégorie créée
        Depot pdg = getOrCreatePdgDepot();
        if (pdg != null && depots.stream().noneMatch(d -> d.getId().equals(pdg.getId()))) {
            depots.add(pdg);
        }

        Categorie categorie = Categorie.builder()
                .nom(request.nom())
                .depots(depots)
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

        List<Long> depotIds = request.getEffectiveDepotIds();
        if (depotIds != null) {
            List<Depot> depots = new ArrayList<>();
            for (Long dId : depotIds) {
                try {
                    Depot d = depotService.getDepotById(dId);
                    if (d != null && !depots.contains(d)) depots.add(d);
                } catch (Exception ignored) {}
            }
            // Maintien automatique du dépôt PDG
            Depot pdg = getOrCreatePdgDepot();
            if (pdg != null && depots.stream().noneMatch(d -> d.getId().equals(pdg.getId()))) {
                depots.add(pdg);
            }
            categorie.setDepots(depots);
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
