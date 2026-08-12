package sn.oas.facturation.mecanicien.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.oas.facturation.mecanicien.data.entity.Mecanicien;
import sn.oas.facturation.mecanicien.dto.MecanicienRequest;
import sn.oas.facturation.mecanicien.repository.MecanicienRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MecanicienServiceImpl implements MecanicienService {

    private final MecanicienRepository mecanicienRepository;
    private final sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService documentNumberGeneratorService;


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


        Mecanicien mecanicien = Mecanicien.builder()
                .numero(documentNumberGeneratorService.generateNextNumber(sn.oas.facturation.shared.documentNumber.DocumentType.MEC))
                .nom(request.getNom())
                .build();
        return mecanicienRepository.save(mecanicien);
    }

    @Override
    public Mecanicien updateMecanicien(Long id, MecanicienRequest request) {
        Mecanicien mecanicien = mecanicienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mécanicien non trouvé"));
        mecanicien.setNom(request.getNom());
        

        
        return mecanicienRepository.save(mecanicien);
    }

    @Override
    public void deleteMecanicien(Long id) {
        mecanicienRepository.deleteById(id);
    }
}
