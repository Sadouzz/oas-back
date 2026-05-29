package sn.oas.facturation.mecanicien.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.oas.facturation.mecanicien.data.entity.Mecanicien;
import sn.oas.facturation.mecanicien.dto.MecanicienRequest;
import sn.oas.facturation.mecanicien.repository.MecanicienRepository;

import java.util.List;
import java.util.Optional;
import sn.oas.facturation.garage.data.entity.Garage;
import sn.oas.facturation.garage.repository.GarageRepository;

@Service
@RequiredArgsConstructor
public class MecanicienServiceImpl implements MecanicienService {

    private final MecanicienRepository mecanicienRepository;
    private final GarageRepository garageRepository;

    @Override
    public List<Mecanicien> getAllMecaniciens() {
        return mecanicienRepository.findAll();
    }

    @Override
    public Optional<Mecanicien> getMecanicienById(Long id) {
        return mecanicienRepository.findById(id);
    }

    @Override
    public Mecanicien createMecanicien(MecanicienRequest request) {
        Garage garage = garageRepository.findById(request.getGarageId())
                .orElseThrow(() -> new RuntimeException("Garage non trouvé"));

        Mecanicien mecanicien = Mecanicien.builder()
                .nom(request.getNom())
                .garage(garage)
                .build();
        return mecanicienRepository.save(mecanicien);
    }

    @Override
    public Mecanicien updateMecanicien(Long id, MecanicienRequest request) {
        Mecanicien mecanicien = mecanicienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mécanicien non trouvé"));
        mecanicien.setNom(request.getNom());
        
        if (request.getGarageId() != null) {
            Garage garage = garageRepository.findById(request.getGarageId())
                    .orElseThrow(() -> new RuntimeException("Garage non trouvé"));
            mecanicien.setGarage(garage);
        }
        
        return mecanicienRepository.save(mecanicien);
    }

    @Override
    public void deleteMecanicien(Long id) {
        mecanicienRepository.deleteById(id);
    }
}
