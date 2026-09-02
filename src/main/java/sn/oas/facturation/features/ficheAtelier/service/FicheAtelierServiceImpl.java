package sn.oas.facturation.features.ficheAtelier.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.auth.data.entity.Client;
import sn.oas.facturation.features.client.repository.ClientRepository;
import sn.oas.facturation.features.ficheAtelier.data.dto.FicheAtelierRequest;
import sn.oas.facturation.features.ficheAtelier.data.dto.FicheAtelierResponse;
import sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.features.ficheAtelier.repository.FicheAtelierRepository;
import sn.oas.facturation.features.rendezvous.data.entity.RendezVous;
import sn.oas.facturation.features.rendezvous.repository.RendezVousRepository;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FicheAtelierServiceImpl implements FicheAtelierService {

    private final FicheAtelierRepository ficheAtelierRepository;
    private final RendezVousRepository rendezVousRepository;
    private final ClientRepository clientRepository;
    private final VehiculeRepository vehiculeRepository;
    private final OrdreReparationRepository ordreReparationRepository;

    @Transactional
    @Override
    public FicheAtelier create(FicheAtelierRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException("Client non trouvé avec l'id : " + request.getClientId()));
        
        Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException("Véhicule non trouvé avec l'id : " + request.getVehiculeId()));

        RendezVous rendezVous = null;
        if (request.getRendezVousId() != null) {
            rendezVous = rendezVousRepository.findById(request.getRendezVousId())
                    .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException("Rendez-vous non trouvé avec l'id : " + request.getRendezVousId()));
                    
            // Check if already exists
            if (ficheAtelierRepository.findByRendezVousId(rendezVous.getId()).isPresent()) {
                throw new sn.oas.facturation.shared.exception.ResourceAlreadyExistsException("Une fiche atelier existe déjà pour ce rendez-vous");
            }
        }

        FicheAtelier fiche = FicheAtelier.builder()
                .rendezVous(rendezVous)
                .client(client)
                .vehicule(vehicule)
                .garage(rendezVous != null ? rendezVous.getGarage() : null) // Get garage from rendezvous
                .nomChauffeur(request.getNomChauffeur())
                .telephoneChauffeur(request.getTelephoneChauffeur())
                .niveauEssence(request.getNiveauEssence())
                .kilometrage(request.getKilometrage())
                .designationTravaux(request.getDesignationTravaux())
                .lignesReception(request.getLignesReception())
                .lignesDefauts(request.getLignesDefauts())
                .nb(request.getNb())
                .dateSortiePrevue(request.getDateSortiePrevue())
                .garantie(request.getGarantie())
                .signatureReceptionnaireBase64(request.getSignatureReceptionnaireBase64())
                .signatureBase64(request.getSignatureBase64())
                .build();

        // If rendezVous is null, we might have issue with garage = null. 
        // We'd need SecurityContext to set garage if not linked to RDV, but here it's linked to RDV.
        if (fiche.getGarage() == null && rendezVous != null) {
            fiche.setGarage(rendezVous.getGarage());
        }

        FicheAtelier saved = ficheAtelierRepository.save(fiche);
        return saved;
    }

    @Transactional
    @Override
    public FicheAtelier update(Long id, FicheAtelierRequest request) {
        FicheAtelier fiche = ficheAtelierRepository.findById(id)
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException("Fiche Atelier non trouvée avec l'id : " + id));

        fiche.setNomChauffeur(request.getNomChauffeur());
        fiche.setTelephoneChauffeur(request.getTelephoneChauffeur());
        fiche.setNiveauEssence(request.getNiveauEssence());
        fiche.setKilometrage(request.getKilometrage());
        fiche.setDesignationTravaux(request.getDesignationTravaux());
        fiche.setLignesReception(request.getLignesReception());
        fiche.setLignesDefauts(request.getLignesDefauts());
        fiche.setNb(request.getNb());
        fiche.setDateSortiePrevue(request.getDateSortiePrevue());
        fiche.setGarantie(request.getGarantie());
        fiche.setSignatureReceptionnaireBase64(request.getSignatureReceptionnaireBase64());
        fiche.setSignatureBase64(request.getSignatureBase64());

        return ficheAtelierRepository.save(fiche);
    }

    @Transactional(readOnly = true)
    @Override
    public FicheAtelier getById(Long id) {
        return ficheAtelierRepository.findById(id)
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException("Fiche Atelier non trouvée avec l'id : " + id));
    }

    @Transactional(readOnly = true)
    @Override
    public FicheAtelier getByRendezVousId(Long rendezVousId) {
        return ficheAtelierRepository.findByRendezVousId(rendezVousId)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<FicheAtelier> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ficheAtelierRepository.findAll(pageable);
    }

    @Override
    public List<FicheAtelier> getAll() {
        return ficheAtelierRepository.findAll();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        if (!ficheAtelierRepository.existsById(id)) {
            throw new sn.oas.facturation.shared.exception.ResourceNotFoundException("Fiche Atelier non trouvée avec l'id : " + id);
        }
        ficheAtelierRepository.deleteById(id);
    }

    @Override
    public FicheAtelier signForExit(Long id, String signature) {
        FicheAtelier fiche = ficheAtelierRepository.findById(id)
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException("Fiche atelier non trouvée avec l'id : " + id));
        fiche.setSignatureSortieBase64(signature);
        return ficheAtelierRepository.save(fiche);
    }
}
