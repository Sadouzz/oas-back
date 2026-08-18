package sn.oas.facturation.ficheAtelier.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.client.repository.ClientRepository;
import sn.oas.facturation.ficheAtelier.data.dto.FicheAtelierRequest;
import sn.oas.facturation.ficheAtelier.data.dto.FicheAtelierResponse;
import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.ficheAtelier.repository.FicheAtelierRepository;
import sn.oas.facturation.rendezvous.data.entity.RendezVous;
import sn.oas.facturation.rendezvous.repository.RendezVousRepository;
import sn.oas.facturation.vehicule.data.entity.Vehicule;
import sn.oas.facturation.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.ordreReparation.repository.OrdreReparationRepository;

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
    public FicheAtelierResponse create(FicheAtelierRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        
        Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));

        RendezVous rendezVous = null;
        if (request.getRendezVousId() != null) {
            rendezVous = rendezVousRepository.findById(request.getRendezVousId())
                    .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé"));
                    
            // Check if already exists
            if (ficheAtelierRepository.findByRendezVousId(rendezVous.getId()).isPresent()) {
                throw new RuntimeException("Une fiche atelier existe déjà pour ce rendez-vous");
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
        return toResponse(saved);
    }

    @Transactional
    @Override
    public FicheAtelierResponse update(Long id, FicheAtelierRequest request) {
        FicheAtelier fiche = ficheAtelierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));

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

        return toResponse(ficheAtelierRepository.save(fiche));
    }

    @Transactional(readOnly = true)
    @Override
    public FicheAtelierResponse getById(Long id) {
        return ficheAtelierRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
    }

    @Transactional(readOnly = true)
    @Override
    public FicheAtelierResponse getByRendezVousId(Long rendezVousId) {
        return ficheAtelierRepository.findByRendezVousId(rendezVousId)
                .map(this::toResponse)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    @Override
    public List<FicheAtelierResponse> getAll() {
        return ficheAtelierRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void delete(Long id) {
        ficheAtelierRepository.deleteById(id);
    }

    @Override
    public FicheAtelierResponse signForExit(Long id, String signature) {
        FicheAtelier fiche = ficheAtelierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fiche atelier non trouvée"));
        fiche.setSignatureSortieBase64(signature);
        return toResponse(ficheAtelierRepository.save(fiche));
    }

    private FicheAtelierResponse toResponse(FicheAtelier fiche) {
        return FicheAtelierResponse.builder()
                .id(fiche.getId())
                .rendezVousId(fiche.getRendezVous() != null ? fiche.getRendezVous().getId() : null)
                .clientId(fiche.getClient() != null ? fiche.getClient().getId() : null)
                .clientName(fiche.getClient() != null ? fiche.getClient().getFirstName() + " " + fiche.getClient().getLastName() : null)
                .vehiculeId(fiche.getVehicule() != null ? fiche.getVehicule().getId() : null)
                .vehiculeImmatriculation(fiche.getVehicule() != null ? fiche.getVehicule().getImmatriculation() : null)
                .garageId(fiche.getGarage() != null ? fiche.getGarage().getId() : null)
                .nomChauffeur(fiche.getNomChauffeur())
                .telephoneChauffeur(fiche.getTelephoneChauffeur())
                .niveauEssence(fiche.getNiveauEssence())
                .kilometrage(fiche.getKilometrage())
                .designationTravaux(fiche.getDesignationTravaux())
                .lignesReception(fiche.getLignesReception())
                .lignesDefauts(fiche.getLignesDefauts())
                .nb(fiche.getNb())
                .dateSortiePrevue(fiche.getDateSortiePrevue())
                .garantie(fiche.getGarantie())
                .signatureReceptionnaireBase64(fiche.getSignatureReceptionnaireBase64())
                .signatureBase64(fiche.getSignatureBase64())
                .signatureSortieBase64(fiche.getSignatureSortieBase64())
                .createdAt(fiche.getCreatedAt())
                .updatedAt(fiche.getUpdatedAt())
                .hasOrdreReparation(ordreReparationRepository.existsByFicheAtelierId(fiche.getId()))
                .build();
    }
}
