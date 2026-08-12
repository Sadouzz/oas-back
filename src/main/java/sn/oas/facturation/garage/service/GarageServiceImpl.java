package sn.oas.facturation.garage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.garage.data.entity.Garage;
import sn.oas.facturation.garage.dto.GarageRequest;
import sn.oas.facturation.garage.dto.GarageResponse;
import sn.oas.facturation.garage.repository.GarageRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GarageServiceImpl implements GarageService {

    private final GarageRepository garageRepository;

    @Override
    @Transactional
    public GarageResponse createGarage(GarageRequest request) {
        if (garageRepository.findByPrefixeIgnoreCase(request.prefixe()).isPresent()) {
            throw new IllegalArgumentException("Un garage avec ce préfixe existe déjà.");
        }
        if (garageRepository.findByNomIgnoreCase(request.nom()).isPresent()) {
            throw new IllegalArgumentException("Un garage avec ce nom existe déjà.");
        }

        Garage garage = Garage.builder()
                .nom(request.nom())
                .localite(request.localite())
                .prefixe(request.prefixe().toUpperCase())
                .numeroFixe(request.numeroFixe())
                .numeroWhatsapp(request.numeroWhatsapp())
                .email(request.email())
                .archived(false)
                .build();

        return GarageResponse.fromEntity(garageRepository.save(garage));
    }

    @Override
    @Transactional
    public GarageResponse updateGarage(Long id, GarageRequest request) {
        Garage garage = getGarageEntityById(id);

        if (!garage.getPrefixe().equalsIgnoreCase(request.prefixe()) &&
                garageRepository.findByPrefixeIgnoreCase(request.prefixe()).isPresent()) {
            throw new IllegalArgumentException("Un garage avec ce préfixe existe déjà.");
        }
        if (!garage.getNom().equalsIgnoreCase(request.nom()) &&
                garageRepository.findByNomIgnoreCase(request.nom()).isPresent()) {
            throw new IllegalArgumentException("Un garage avec ce nom existe déjà.");
        }

        garage.setNom(request.nom());
        garage.setLocalite(request.localite());
        garage.setPrefixe(request.prefixe().toUpperCase());
        garage.setNumeroFixe(request.numeroFixe());
        garage.setNumeroWhatsapp(request.numeroWhatsapp());
        garage.setEmail(request.email());

        return GarageResponse.fromEntity(garageRepository.save(garage));
    }

    @Override
    @Transactional(readOnly = true)
    public GarageResponse getGarageById(Long id) {
        return GarageResponse.fromEntity(getGarageEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GarageResponse> getAllGarages(boolean includeArchived) {
        return garageRepository.findAll().stream()
                .filter(g -> includeArchived || !g.isArchived())
                .map(GarageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteGarage(Long id) {
        Garage garage = getGarageEntityById(id);
        garage.setArchived(true);
        garageRepository.save(garage);
    }

    @Override
    @Transactional
    public void restoreGarage(Long id) {
        Garage garage = getGarageEntityById(id);
        garage.setArchived(false);
        garageRepository.save(garage);
    }

    @Override
    @Transactional(readOnly = true)
    public Garage getGarageEntityById(Long id) {
        return garageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Garage non trouvé avec l'id : " + id));
    }
}
