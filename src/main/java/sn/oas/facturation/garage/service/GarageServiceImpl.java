package sn.oas.facturation.garage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.oas.facturation.garage.data.entity.Garage;
import sn.oas.facturation.garage.dto.GarageRequest;
import sn.oas.facturation.garage.repository.GarageRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GarageServiceImpl implements GarageService {

    private final GarageRepository garageRepository;

    @Override
    public Garage createGarage(GarageRequest request) {
        Garage garage = Garage.builder()
                .libelle(request.getLibelle())
                .ville(request.getVille())
                .adresse(request.getAdresse())
                .contact(request.getContact())
                .build();
        return garageRepository.save(garage);
    }

    @Override
    public List<Garage> getAllGarages() {
        return garageRepository.findAll();
    }

    @Override
    public Optional<Garage> getGarageById(Long id) {
        return garageRepository.findById(id);
    }

    @Override
    public Garage updateGarage(Long id, GarageRequest request) {
        Garage garage = garageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Garage non trouvé"));
        
        if (request.getLibelle() != null) garage.setLibelle(request.getLibelle());
        if (request.getVille() != null) garage.setVille(request.getVille());
        if (request.getAdresse() != null) garage.setAdresse(request.getAdresse());
        if (request.getContact() != null) garage.setContact(request.getContact());
        
        return garageRepository.save(garage);
    }

    @Override
    public void deleteGarage(Long id) {
        Garage garage = garageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Garage non trouvé"));
        garageRepository.delete(garage);
    }
}
