package sn.oas.facturation.features.ficheAtelier.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.repository.ClientRepository;
import sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.features.ficheAtelier.dto.FicheAtelierRequest;
import sn.oas.facturation.features.ficheAtelier.repository.FicheAtelierRepository;
import sn.oas.facturation.features.rendezvous.data.entity.RendezVous;
import sn.oas.facturation.features.rendezvous.data.enums.RendezVousStatus;
import sn.oas.facturation.features.rendezvous.repository.RendezVousRepository;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService;
import sn.oas.facturation.shared.documentNumber.DocumentType;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;
import sn.oas.facturation.shared.exception.BadRequestException;
import sn.oas.facturation.shared.exception.ResourceAlreadyExistsException;
import sn.oas.facturation.features.garage.data.entity.Garage;

@Service
@RequiredArgsConstructor
public class FicheAtelierServiceImpl implements FicheAtelierService {

    private final FicheAtelierRepository ficheAtelierRepository;
    private final RendezVousRepository rendezVousRepository;
    private final ClientRepository clientRepository;
    private final VehiculeRepository vehiculeRepository;
    private final OrdreReparationRepository ordreReparationRepository;
    private final sn.oas.facturation.features.ordreReparation.service.OrdreReparationService ordreReparationService;
    private final DocumentNumberGeneratorService documentNumberGeneratorService;
    private final ApplicationEventPublisher eventPublisher;
    private final FicheAtelierPdfService ficheAtelierPdfService;
    private final sn.oas.facturation.features.ficheAtelier.repository.FicheAtelierPdfRepository pdfRepository;
    private final sn.oas.facturation.features.notification.service.AgentNotificationService agentNotificationService;

    @Transactional
    @Override
    public FicheAtelier create(FicheAtelierRequest request) {
        verifierSignature(request.getSignatureReceptionnaireBase64());
        verifierSignature(request.getSignatureBase64());
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client non trouvé avec l'id : " + request.getClientId()));

        Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Véhicule non trouvé avec l'id : " + request.getVehiculeId()));
        if (vehicule.getClient() == null || !client.getId().equals(vehicule.getClient().getId())) {
            throw new BadRequestException("Le véhicule n'est pas attribué au client indiqué.");
        }
        if (!vehicule.isActif()) {
            throw new BadRequestException("Ce véhicule est en attente d'activation par un agent.");
        }

        // BLOCAGE : Si le véhicule a un ordre de réparation qui n'est pas encore livré
        Optional<OrdreReparation> ordreEnCours =
                ordreReparationRepository.findFirstByVehiculeIdAndStatutNotIn(
                        vehicule.getId(), List.of(StatutOrdreReparation.LIVRE));
        if (ordreEnCours.isPresent()) {
            OrdreReparation or = ordreEnCours.get();
            // Si cet ordre est un ordre orphelin (sans fiche atelier) issu de l'ancienne validation de RDV, on le supprime
            if (or.getFicheAtelier() == null) {
                ordreReparationRepository.delete(or);
            } else {
                String statutLabel = or.getStatut() != null ? or.getStatut().getLabel() : "en cours";
                throw new BadRequestException(
                        "Impossible de créer une fiche atelier : le véhicule " + vehicule.getImmatriculation()
                        + " a déjà un ordre de réparation en cours (" + or.getNumero()
                        + " - Statut : " + statutLabel + ") et n'est pas encore livré.");
            }
        }

        RendezVous rendezVous = null;
        if (request.getRendezVousId() != null) {
            rendezVous = rendezVousRepository.findById(request.getRendezVousId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Rendez-vous non trouvé avec l'id : " + request.getRendezVousId()));
            if (rendezVous.getClient() == null || !client.getId().equals(rendezVous.getClient().getId())
                    || rendezVous.getVehicule() == null || !vehicule.getId().equals(rendezVous.getVehicule().getId())) {
                throw new BadRequestException("Le rendez-vous ne correspond pas au client et au véhicule indiqués.");
            }

            // Check if already exists
            if (ficheAtelierRepository.findByRendezVousId(rendezVous.getId()).isPresent()) {
                throw new ResourceAlreadyExistsException(
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
                .signatureReceptionnaireBase64(request.getSignatureReceptionnaireBase64())
                .signatureBase64(request.getSignatureBase64())
                .build();

        FicheAtelier saved = ficheAtelierRepository.save(fiche);
        ficheAtelierRepository.flush();
        pdfRepository.save(new sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelierPdf(
                saved.getId(), ficheAtelierPdfService.generer(saved.getId())));
        if (rendezVous != null) {
            rendezVous.setFicheAtelier(saved);
            rendezVous.setStatut(RendezVousStatus.TERMINE);
            rendezVousRepository.save(rendezVous);
        }
        
        agentNotificationService.notifyRole(sn.oas.facturation.features.user.data.enums.Role.CHEF_ATELIER,
                "Nouvelle Fiche Atelier",
                "Une nouvelle fiche atelier (" + saved.getNumero() + ") a été créée.");
        eventPublisher.publishEvent(new FicheAtelierCreee(saved.getId()));
                
        return saved;
    }

    private static void verifierSignature(String signature) {
        if (signature == null || signature.length() > 1_500_000
                || !signature.matches("^data:image/png;base64,[A-Za-z0-9+/=]+$")) {
            throw new BadRequestException("Les deux signatures PNG sont obligatoires sur la fiche atelier.");
        }
    }

    @Transactional
    @Override
    public FicheAtelier update(Long id, FicheAtelierRequest request) {
        FicheAtelier fiche = ficheAtelierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fiche Atelier non trouvée avec l'id : " + id));

        fiche.setNomChauffeur(request.getNomChauffeur());
        fiche.setTelephoneChauffeur(request.getTelephoneChauffeur());
        fiche.setNiveauEssence(request.getNiveauEssence());
        fiche.setKilometrage(request.getKilometrage());
        fiche.setDesignationTravaux(request.getDesignationTravaux());
        fiche.setLignesReception(request.getLignesReception());
        fiche.setLignesDefauts(request.getLignesDefauts());
        fiche.setNb(request.getNb());
        fiche.setSignatureReceptionnaireBase64(request.getSignatureReceptionnaireBase64());
        fiche.setSignatureBase64(request.getSignatureBase64());

        return ficheAtelierRepository.save(fiche);
    }

    @Transactional(readOnly = true)
    @Override
    public FicheAtelier getById(Long id) {
        return ficheAtelierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
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
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("updatedAt").nullsLast(),
                Sort.Order.desc("createdAt").nullsLast(),
                Sort.Order.desc("id")
        ));
        return ficheAtelierRepository.findAll(pageable);
    }

    @Override
    public List<FicheAtelier> getAll() {
        return ficheAtelierRepository.findAll(Sort.by(
                Sort.Order.desc("updatedAt").nullsLast(),
                Sort.Order.desc("createdAt").nullsLast(),
                Sort.Order.desc("id")
        ));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        FicheAtelier fiche = ficheAtelierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fiche Atelier non trouvée avec l'id : " + id));
        if (fiche.getRendezVous() != null) {
            RendezVous rv = fiche.getRendezVous();
            rv.setFicheAtelier(null);
            rv.setStatut(RendezVousStatus.CONFIRME);
            rendezVousRepository.save(rv);
        }
        if (pdfRepository.existsById(id)) pdfRepository.deleteById(id);
        ficheAtelierRepository.delete(fiche);
    }

    @Override
    @Transactional
    public FicheAtelier signForExit(Long id, String signature) {
        FicheAtelier fiche = ficheAtelierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fiche atelier non trouvée avec l'id : " + id));
        if (fiche.getOrdreReparation() == null) {
            throw new BadRequestException("Aucun ordre de réparation associé à cette fiche.");
        }
        ordreReparationService.restituerVehicule(fiche.getOrdreReparation().getId(), signature, 1);
        return ficheAtelierRepository.findById(id).orElseThrow();
    }

    @Override 
    public boolean existsByOrdreReparationId(Long ordreReparationId) {
        return ficheAtelierRepository.existsByOrdreReparationId(ordreReparationId);
    }

    @Override
    public boolean isVehiculeEnReparationNonLivre(Long vehiculeId) {
        if (vehiculeId == null) return false;
        return ordreReparationRepository.findFirstByVehiculeIdAndStatutNotIn(
                vehiculeId, java.util.List.of(sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation.LIVRE))
                .filter(or -> or.getFicheAtelier() != null)
                .isPresent();
    }
}
