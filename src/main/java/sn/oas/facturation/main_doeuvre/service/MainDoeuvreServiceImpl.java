package sn.oas.facturation.main_doeuvre.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.main_doeuvre.data.entity.MainDoeuvre;
import sn.oas.facturation.main_doeuvre.dto.MainDoeuvreRequest;
import sn.oas.facturation.main_doeuvre.repository.MainDoeuvreRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MainDoeuvreServiceImpl implements MainDoeuvreService {

    private final MainDoeuvreRepository mainDoeuvreRepository;

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
        MainDoeuvre mainDoeuvre = MainDoeuvre.builder()
                .prix(request.prix())
                .categorie(request.categorie())
                .nbreHeure(request.nbreHeure())
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
        if (request.categorie() != null) {
            mainDoeuvre.setCategorie(request.categorie());
        }
        if (request.nbreHeure() != null) {
            mainDoeuvre.setNbreHeure(request.nbreHeure());
        }

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
