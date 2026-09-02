package sn.oas.facturation.features.garage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.features.garage.dto.GarageRequest;
import sn.oas.facturation.features.garage.dto.GarageResponse;
import sn.oas.facturation.features.garage.repository.GarageRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GarageServiceImpl implements GarageService {

    private final GarageRepository garageRepository;

    @Override
    @Transactional
    public Garage createGarage(GarageRequest request) {
        if (garageRepository.findByPrefixeIgnoreCase(request.prefixe()).isPresent()) {
            throw new sn.oas.facturation.shared.exception.ResourceAlreadyExistsException("Un garage avec ce préfixe existe déjà.");
        }
        if (garageRepository.findByNomIgnoreCase(request.nom()).isPresent()) {
            throw new sn.oas.facturation.shared.exception.ResourceAlreadyExistsException("Un garage avec ce nom existe déjà.");
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

        return garageRepository.save(garage);
    }

    @Override
    @Transactional
    public Garage updateGarage(Long id, GarageRequest request) {
        Garage garage = getGarageEntityById(id);

        if (!garage.getPrefixe().equalsIgnoreCase(request.prefixe()) &&
                garageRepository.findByPrefixeIgnoreCase(request.prefixe()).isPresent()) {
            throw new sn.oas.facturation.shared.exception.ResourceAlreadyExistsException("Un garage avec ce préfixe existe déjà.");
        }
        if (!garage.getNom().equalsIgnoreCase(request.nom()) &&
                garageRepository.findByNomIgnoreCase(request.nom()).isPresent()) {
            throw new sn.oas.facturation.shared.exception.ResourceAlreadyExistsException("Un garage avec ce nom existe déjà.");
        }

        garage.setNom(request.nom());
        garage.setLocalite(request.localite());
        garage.setPrefixe(request.prefixe().toUpperCase());
        garage.setNumeroFixe(request.numeroFixe());
        garage.setNumeroWhatsapp(request.numeroWhatsapp());
        garage.setEmail(request.email());

        return garageRepository.save(garage);
    }

    @Override
    @Transactional(readOnly = true)
    public Garage getGarageById(Long id) {
        return getGarageEntityById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Garage> getAllGarages(boolean includeArchived, int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return includeArchived
                ? garageRepository.findAll(pageable)
                : garageRepository.findByArchivedFalse(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Garage> getAllGarages(boolean includeArchived) {
        return garageRepository.findAll().stream()
                .filter(g -> includeArchived || !g.isArchived())
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
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException("Garage non trouvé avec l'id : " + id));
    }
}
