package sn.oas.facturation.ficheAtelier.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.ficheAtelier.dto.FicheAtelierRequest;
import sn.oas.facturation.ficheAtelier.repository.FicheAtelierRepository;
import sn.oas.facturation.vehicule.data.entity.Vehicule;
import sn.oas.facturation.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.mecanicien.data.entity.Mecanicien;
import sn.oas.facturation.mecanicien.repository.MecanicienRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FicheAtelierServiceImpl implements FicheAtelierService {

    private final FicheAtelierRepository ficheAtelierRepository;
    private final VehiculeRepository vehiculeRepository;
    private final MecanicienRepository mecanicienRepository;

    @Override
    public FicheAtelier createFicheAtelier(FicheAtelierRequest request) {
        Vehicule vehicule = null;
        if (request.getVehiculeId() != null) {
            vehicule = vehiculeRepository.findById(request.getVehiculeId())
                    .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));
        } else {
            throw new RuntimeException("L'ID du véhicule est obligatoire");
        }

        FicheAtelier ficheAtelier = FicheAtelier.builder()
                .numero(request.getNumero())
                .descriptionTravaux(request.getDescriptionTravaux())
                .listeReception(request.getListeReception())
                .listeDefauts(request.getListeDefauts())
                .dateSortie(request.getDateSortie())
                .vehicule(vehicule)
                .build();
        return ficheAtelierRepository.save(ficheAtelier);
    }

    @Override
    public List<FicheAtelier> getAllFichesAtelier() {
        return ficheAtelierRepository.findAll();
    }

    @Override
    public Optional<FicheAtelier> getFicheAtelierById(Long id) {
        return ficheAtelierRepository.findById(id);
    }

    @Override
    public FicheAtelier updateFicheAtelier(Long id, FicheAtelierRequest request) {
        FicheAtelier ficheAtelier = ficheAtelierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        
        if (request.getNumero() != null) ficheAtelier.setNumero(request.getNumero());
        if (request.getDescriptionTravaux() != null) ficheAtelier.setDescriptionTravaux(request.getDescriptionTravaux());
        if (request.getListeReception() != null) ficheAtelier.setListeReception(request.getListeReception());
        if (request.getListeDefauts() != null) ficheAtelier.setListeDefauts(request.getListeDefauts());
        if (request.getDateSortie() != null) ficheAtelier.setDateSortie(request.getDateSortie());

        if (request.getVehiculeId() != null) {
            Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                    .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));
            ficheAtelier.setVehicule(vehicule);
        }
        
        return ficheAtelierRepository.save(ficheAtelier);
    }

    @Override
    public void deleteFicheAtelier(Long id) {
        FicheAtelier ficheAtelier = ficheAtelierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        ficheAtelierRepository.delete(ficheAtelier);
    }

    @Transactional
    @Override
    public void assignMecanicien(Long ficheId, Long mecanicienId) {
        FicheAtelier fiche = ficheAtelierRepository.findById(ficheId)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        Mecanicien mecanicien = mecanicienRepository.findById(mecanicienId)
                .orElseThrow(() -> new RuntimeException("Mécanicien non trouvé"));

        if (!fiche.getMecaniciens().contains(mecanicien)) {
            fiche.getMecaniciens().add(mecanicien);
            ficheAtelierRepository.save(fiche);
        }
    }

    @Transactional
    @Override
    public void removeMecanicien(Long ficheId, Long mecanicienId) {
        FicheAtelier fiche = ficheAtelierRepository.findById(ficheId)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        Mecanicien mecanicien = mecanicienRepository.findById(mecanicienId)
                .orElseThrow(() -> new RuntimeException("Mécanicien non trouvé"));

        fiche.getMecaniciens().remove(mecanicien);
        ficheAtelierRepository.save(fiche);
    }
}
