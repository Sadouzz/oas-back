package sn.oas.facturation.main_doeuvre.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.main_doeuvre.data.entity.MainDoeuvre;
import sn.oas.facturation.main_doeuvre.dto.MainDoeuvreRequest;
import sn.oas.facturation.main_doeuvre.repository.MainDoeuvreRepository;

import sn.oas.facturation.main_doeuvre.repository.CategorieMainDoeuvreRepository;
import sn.oas.facturation.main_doeuvre.data.entity.CategorieMainDoeuvre;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MainDoeuvreServiceImpl implements MainDoeuvreService {

    private final MainDoeuvreRepository mainDoeuvreRepository;
    private final CategorieMainDoeuvreRepository categorieMainDoeuvreRepository;

    @Override
    public List<MainDoeuvre> getAllMainDoeuvres() {
        return mainDoeuvreRepository.findAll();
    }

    @Override
    public MainDoeuvre getMainDoeuvreById(Long id) {
        return mainDoeuvreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Main d'oeuvre non trouvée"));
    }

    @Transactional
    @Override
    public MainDoeuvre createMainDoeuvre(MainDoeuvreRequest request) {
        CategorieMainDoeuvre categorie = categorieMainDoeuvreRepository.findById(request.categorieId())
                .orElseThrow(() -> new RuntimeException("Catégorie de main d'oeuvre non trouvée"));

        MainDoeuvre mainDoeuvre = MainDoeuvre.builder()
                .prix(request.prix())
                .categorie(categorie)
                .nbreHeure(request.nbreHeure())
                .isArchived(request.isArchived() != null ? request.isArchived() : false)
                .build();
        return mainDoeuvreRepository.save(mainDoeuvre);
    }

    @Transactional
    @Override
    public MainDoeuvre updateMainDoeuvre(Long id, MainDoeuvreRequest request) {
        MainDoeuvre mainDoeuvre = mainDoeuvreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Main d'oeuvre non trouvée"));

        if (request.prix() != null) {
            mainDoeuvre.setPrix(request.prix());
        }
        if (request.categorieId() != null) {
            CategorieMainDoeuvre categorie = categorieMainDoeuvreRepository.findById(request.categorieId())
                    .orElseThrow(() -> new RuntimeException("Catégorie de main d'oeuvre non trouvée"));
            mainDoeuvre.setCategorie(categorie);
        }
        if (request.nbreHeure() != null) {
            mainDoeuvre.setNbreHeure(request.nbreHeure());
        }
        if (request.isArchived() != null) {
            mainDoeuvre.setIsArchived(request.isArchived());
        }

        return mainDoeuvreRepository.save(mainDoeuvre);
    }

    @Transactional
    @Override
    public MainDoeuvre archiveMainDoeuvre(Long id, boolean archived) {
        MainDoeuvre mainDoeuvre = mainDoeuvreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Main d'oeuvre non trouvée"));
        mainDoeuvre.setIsArchived(archived);
        return mainDoeuvreRepository.save(mainDoeuvre);
    }

    @Transactional
    @Override
    public void deleteMainDoeuvre(Long id) {
        if (!mainDoeuvreRepository.existsById(id)) {
            throw new RuntimeException("Main d'oeuvre non trouvée");
        }
        mainDoeuvreRepository.deleteById(id);
    }
}
