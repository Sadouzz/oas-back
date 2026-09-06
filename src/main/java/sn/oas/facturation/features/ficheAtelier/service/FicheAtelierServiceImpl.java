package sn.oas.facturation.features.ficheAtelier.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.repository.ClientRepository;
import sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.features.ficheAtelier.dto.FicheAtelierRequest;
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

import sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService;
import sn.oas.facturation.shared.documentNumber.DocumentType;
import sn.oas.facturation.features.garage.data.entity.Garage;

@Service
@RequiredArgsConstructor
public class FicheAtelierServiceImpl implements FicheAtelierService {

    private final FicheAtelierRepository ficheAtelierRepository;
    private final RendezVousRepository rendezVousRepository;
    private final ClientRepository clientRepository;
    private final VehiculeRepository vehiculeRepository;
    private final OrdreReparationRepository ordreReparationRepository;
    private final DocumentNumberGeneratorService documentNumberGeneratorService;

    @Transactional
    @Override
    public FicheAtelier create(FicheAtelierRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException(
                        "Client non trouvé avec l'id : " + request.getClientId()));

        Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException(
                        "Véhicule non trouvé avec l'id : " + request.getVehiculeId()));

        RendezVous rendezVous = null;
        if (request.getRendezVousId() != null) {
            rendezVous = rendezVousRepository.findById(request.getRendezVousId())
                    .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException(
                            "Rendez-vous non trouvé avec l'id : " + request.getRendezVousId()));

            // Check if already exists
            if (ficheAtelierRepository.findByRendezVousId(rendezVous.getId()).isPresent()) {
                throw new sn.oas.facturation.shared.exception.ResourceAlreadyExistsException(
                        "Une fiche atelier existe déjà pour ce rendez-vous");
            }
        }

        Garage garage = (rendezVous != null && rendezVous.getGarage() != null)
                ? rendezVous.getGarage()
                : documentNumberGeneratorService.getCurrentGarage();

        String numero = documentNumberGeneratorService.generateNextNumber(garage, DocumentType.FA);

        FicheAtelier fiche = FicheAtelier.builder()
                .numero(numero)
                .rendezVous(rendezVous)
                .client(client)
                .vehicule(vehicule)
                .garage(garage)
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

        FicheAtelier saved = ficheAtelierRepository.save(fiche);
        if (rendezVous != null) {
            rendezVous.setFicheAtelier(saved);
            rendezVousRepository.save(rendezVous);
        }
        return saved;
    }

    @Transactional
    @Override
    public FicheAtelier update(Long id, FicheAtelierRequest request) {
        FicheAtelier fiche = ficheAtelierRepository.findById(id)
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException(
                        "Fiche Atelier non trouvée avec l'id : " + id));

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
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException(
                        "Fiche Atelier non trouvée avec l'id : " + id));
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
        FicheAtelier fiche = ficheAtelierRepository.findById(id)
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException("Fiche Atelier non trouvée avec l'id : " + id));
        if (fiche.getRendezVous() != null) {
            RendezVous rv = fiche.getRendezVous();
            rv.setFicheAtelier(null);
            rendezVousRepository.save(rv);
        }
        ficheAtelierRepository.delete(fiche);
    }

    @Override
    public FicheAtelier signForExit(Long id, String signature) {
        FicheAtelier fiche = ficheAtelierRepository.findById(id)
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException(
                        "Fiche atelier non trouvée avec l'id : " + id));
        fiche.setSignatureSortieBase64(signature);
        return ficheAtelierRepository.save(fiche);
    }

    @Override 
    public boolean existsByOrdreReparationId(Long ordreReparationId) {
        return ficheAtelierRepository.existsByOrdreReparationId(ordreReparationId);
    }
}
