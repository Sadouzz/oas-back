package sn.oas.facturation.features.rendezvous.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.repository.ClientRepository;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.features.garage.repository.GarageRepository;
import sn.oas.facturation.features.notification.service.NotificationService;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.features.ordreReparation.dto.OrdreReparationRequest;
import sn.oas.facturation.features.ordreReparation.service.OrdreReparationService;
import sn.oas.facturation.features.rendezvous.data.entity.RendezVous;
import sn.oas.facturation.features.rendezvous.data.enums.RendezVousStatus;
import sn.oas.facturation.features.rendezvous.dto.RendezVousRequest;
import sn.oas.facturation.features.rendezvous.repository.RendezVousRepository;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService;
import sn.oas.facturation.shared.documentNumber.DocumentType;
import sn.oas.facturation.shared.exception.BadRequestException;
import sn.oas.facturation.shared.exception.ForbiddenException;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RendezVousServiceImpl implements RendezVousService {

    private final RendezVousRepository rendezvousRepository;
    private final VehiculeRepository vehiculeRepository;
    private final ClientRepository clientRepository;
    private final GarageRepository garageRepository;
    private final NotificationService notificationService;
    private final OrdreReparationService ordreReparationService;
    private final DocumentNumberGeneratorService documentNumberGeneratorService;

    @Transactional
    @Override
    public RendezVous createRendezVousByAdmin(RendezVousRequest request) {
        if (request.dateRendezVous() == null) {
            throw new BadRequestException("La date du rendez-vous est obligatoire");
        }
        if (request.dateRendezVous().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("La date du rendez-vous ne peut pas être dans le passé");
        }

        Vehicule vehicule = null;
        if (request.vehiculeId() != null) {
            vehicule = vehiculeRepository.findById(request.vehiculeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Véhicule non trouvé avec l'identifiant " + request.vehiculeId()));
        }

        Client client = null;
        if (request.clientId() != null) {
            client = clientRepository.findById(request.clientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'identifiant " + request.clientId()));
        } else if (vehicule != null && vehicule.getClient() != null) {
            client = vehicule.getClient();
        } else {
            throw new BadRequestException("Veuillez spécifier un client ou sélectionner un véhicule associé à un client");
        }

        if (vehicule != null && vehicule.getClient() != null && !vehicule.getClient().getId().equals(client.getId())) {
            throw new BadRequestException("Le véhicule sélectionné n'appartient pas au client sélectionné");
        }

        if (vehicule != null) {
            if (rendezvousRepository.existsByVehiculeIdAndStatut(vehicule.getId(), RendezVousStatus.EN_ATTENTE)) {
                throw new BadRequestException("Ce véhicule a déjà un rendez-vous en attente");
            }
            if (rendezvousRepository.existsByVehiculeIdAndStatutAndDateRendezVousAfter(vehicule.getId(), RendezVousStatus.CONFIRME, LocalDateTime.now())) {
                throw new BadRequestException("Ce véhicule a déjà un rendez-vous confirmé à venir");
            }
            if (ordreReparationService.existsByVehiculeIdAndStatutNotIn(vehicule.getId(),
                    List.of(StatutOrdreReparation.PRET_A_LIVRER, StatutOrdreReparation.LIVRE))) {
                throw new BadRequestException("Ce véhicule est actuellement en cours d'intervention/réparation");
            }
        }

        Garage garage = null;
        if (request.garageId() != null) {
            garage = garageRepository.findById(request.garageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Garage non trouvé avec l'identifiant " + request.garageId()));
        } else {
            garage = documentNumberGeneratorService.getCurrentGarage();
            if (garage == null) {
                garage = garageRepository.findAll().stream().findFirst().orElse(null);
            }
        }

        RendezVousStatus statut = request.statut() != null ? request.statut() : RendezVousStatus.CONFIRME;

        RendezVous rv = RendezVous.builder()
                .numero(documentNumberGeneratorService.generateNextNumber(garage, DocumentType.RDV))
                .client(client)
                .vehicule(vehicule)
                .garage(garage)
                .dateRendezVous(request.dateRendezVous())
                .motif(request.motif())
                .statut(statut)
                .commentaire(request.commentaire())
                .build();

        rendezvousRepository.save(rv);

        // Notify client
        if (client != null) {
            notificationService.sendNotification(client, "Rendez-vous programmé",
                    "Un rendez-vous a été programmé pour vous le " + request.dateRendezVous() +
                            (request.motif() != null && !request.motif().isBlank() ? " (" + request.motif() + ")" : "") + ".");
        }

        return rv;
    }

    @Transactional
    @Override
    public RendezVous bookRendezVous(Client client, RendezVousRequest request) {
        if (request.vehiculeId() == null) {
            throw new BadRequestException("Veuillez sélectionner un véhicule");
        }
        
        if (request.dateRendezVous() != null && request.dateRendezVous().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("La date du rendez-vous ne peut pas être dans le passé");
        }
        
        Vehicule vehicule = vehiculeRepository.findById(request.vehiculeId())
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule non trouvé avec l'identifiant " + request.vehiculeId()));
        if (!vehicule.getClient().getId().equals(client.getId())) {
            throw new ForbiddenException("Le véhicule n'appartient pas au client connecté");
        }
        if (rendezvousRepository.existsByVehiculeIdAndStatut(vehicule.getId(), RendezVousStatus.EN_ATTENTE)) {
            throw new BadRequestException("Ce véhicule a déjà un rendez-vous en attente");
        }
        
        if (rendezvousRepository.existsByVehiculeIdAndStatutAndDateRendezVousAfter(vehicule.getId(), RendezVousStatus.CONFIRME, LocalDateTime.now())) {
            throw new BadRequestException("Ce véhicule a déjà un rendez-vous confirmé à venir");
        }

        if (ordreReparationService.existsByVehiculeIdAndStatutNotIn(vehicule.getId(), 
                List.of(StatutOrdreReparation.PRET_A_LIVRER, StatutOrdreReparation.LIVRE))) {
            throw new BadRequestException("Ce véhicule est actuellement en cours d'intervention/réparation");
        }

        Garage garage = null;
        if (request.garageId() != null) {
            garage = garageRepository.findById(request.garageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Garage non trouvé"));
        } else {
            throw new BadRequestException("Veuillez sélectionner un garage");
        }

        RendezVous rv = RendezVous.builder()
                .numero(documentNumberGeneratorService.generateNextNumber(garage, DocumentType.RDV))
                .client(client)
                .vehicule(vehicule)
                .garage(garage)
                .dateRendezVous(request.dateRendezVous())
                .motif(request.motif())
                .statut(RendezVousStatus.EN_ATTENTE)
                .build();

        rendezvousRepository.save(rv);

        // Notify client
        notificationService.sendNotification(client, "Rendez-vous enregistré", 
                "Votre demande de rendez-vous pour le " + request.dateRendezVous() + " a bien été enregistrée et est en attente de confirmation.");

        return rv;
    }

    @Transactional
    @Override
    public RendezVous cancelRendezVous(Client client, Long id) {
        RendezVous rv = rendezvousRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'identifiant " + id));
        if (!rv.getClient().getId().equals(client.getId())) {
            throw new ForbiddenException("Accès non autorisé à ce rendez-vous");
        }
        rv.setStatut(RendezVousStatus.ANNULE);
        rendezvousRepository.save(rv);

        notificationService.sendNotification(client, "Rendez-vous annulé", 
                "Vous avez annulé votre rendez-vous du " + rv.getDateRendezVous());

        return rv;
    }

    @Transactional(readOnly = true)
    @Override
    public List<RendezVous> getClientRendezVous(Client client) {
        return getRendezVousByClientId(client.getId());
    }

    @Transactional(readOnly = true)
    @Override
    public Page<RendezVous> getClientRendezVous(Client client, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateCreation").descending());
        return rendezvousRepository.findByClientId(client.getId(), pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public List<RendezVous> getRendezVousByClientId(Long clientId) {
        return rendezvousRepository.findByClientIdOrderByDateCreationDesc(clientId);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<RendezVous> getRendezVousByClientId(Long clientId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateCreation").descending());
        return rendezvousRepository.findByClientId(clientId, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public List<RendezVous> getClientRendezVousByStatus(Client client, RendezVousStatus status) {
        return rendezvousRepository.findByClientIdAndStatutOrderByDateCreationDesc(client.getId(), status);
    }

    @Transactional(readOnly = true)
    @Override
    public RendezVous getById(Long id) {
        return rendezvousRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'identifiant " + id));
    }

    @Transactional(readOnly = true)
    @Override
    public List<RendezVous> getAllRendezVous() {
        return rendezvousRepository.findAllByOrderByDateCreationDesc();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<RendezVous> getAllRendezVous(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateCreation").descending());
        return rendezvousRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<RendezVous> searchRendezVous(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateCreation").descending());
        return rendezvousRepository.searchRendezVous(keyword, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<RendezVous> getByStatut(RendezVousStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateCreation").descending());
        return rendezvousRepository.findByStatut(status, pageable);
    }

    @Transactional
    @Override
    public RendezVous updateRendezVousStatus(Long id, RendezVousStatus status, String commentaire) {
        RendezVous rv = rendezvousRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'identifiant " + id));
        rv.setStatut(status);
        if (commentaire != null) {
            rv.setCommentaire(commentaire);
        }
        rendezvousRepository.save(rv);

        // Notify client
        String titre = "Mise à jour du rendez-vous";
        String message = "Votre rendez-vous du " + rv.getDateRendezVous() + " est maintenant " + status.name() + ".";
        if (commentaire != null && !commentaire.trim().isEmpty()) {
            message += " Commentaire : " + commentaire;
        }
        notificationService.sendNotification(rv.getClient(), titre, message);

        return rv;
    }

    @Transactional
    @Override
    public RendezVous validerRendezVous(Long id, List<Long> mecanicienIds) {
        RendezVous rv = rendezvousRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'identifiant " + id));
        if (rv.getVehicule() == null) {
            throw new BadRequestException("Impossible de valider un rendez-vous sans véhicule associé.");
        }
        
        rv.setStatut(RendezVousStatus.CONFIRME);
        rendezvousRepository.save(rv);

        notificationService.sendNotification(rv.getClient(), "Rendez-vous validé", 
                "Votre rendez-vous du " + rv.getDateRendezVous() + " a été validé et confirmé.");

        return rv;
    }

    @Transactional
    @Override
    public RendezVous updateDate(Long id, LocalDateTime nouvelleDate) {
        RendezVous rv = rendezvousRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'identifiant " + id));
        if (nouvelleDate == null) {
            throw new BadRequestException("La date du rendez-vous est obligatoire");
        }
        if (nouvelleDate.toLocalDate().isBefore(java.time.LocalDate.now())) {
            throw new BadRequestException("La nouvelle date ne peut pas être dans le passé");
        }
        rv.setDateRendezVous(nouvelleDate);
        rendezvousRepository.save(rv);

        if (rv.getClient() != null) {
            notificationService.sendNotification(rv.getClient(), "Date de rendez-vous modifiée",
                    "La date de votre rendez-vous a été modifiée au " + nouvelleDate + ".");
        }

        return rv;
    }

    @Transactional
    @Override
    public RendezVous updateRendezVous(Long id, RendezVousRequest request) {
        RendezVous rv = rendezvousRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'identifiant " + id));

        if (request.dateRendezVous() != null) {
            if (request.dateRendezVous().toLocalDate().isBefore(java.time.LocalDate.now())) {
                throw new BadRequestException("La date du rendez-vous ne peut pas être dans le passé");
            }
            rv.setDateRendezVous(request.dateRendezVous());
        }

        if (request.motif() != null) {
            rv.setMotif(request.motif());
        }

        if (request.commentaire() != null) {
            rv.setCommentaire(request.commentaire());
        }

        if (request.statut() != null) {
            rv.setStatut(request.statut());
        }

        if (request.vehiculeId() != null && (rv.getVehicule() == null || !request.vehiculeId().equals(rv.getVehicule().getId()))) {
            Vehicule vehicule = vehiculeRepository.findById(request.vehiculeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Véhicule non trouvé avec l'identifiant " + request.vehiculeId()));
            rv.setVehicule(vehicule);
        }

        if (request.clientId() != null && (rv.getClient() == null || !request.clientId().equals(rv.getClient().getId()))) {
            Client client = clientRepository.findById(request.clientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'identifiant " + request.clientId()));
            rv.setClient(client);
        }

        if (request.garageId() != null && (rv.getGarage() == null || !request.garageId().equals(rv.getGarage().getId()))) {
            Garage garage = garageRepository.findById(request.garageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Garage non trouvé avec l'identifiant " + request.garageId()));
            rv.setGarage(garage);
        }

        rendezvousRepository.save(rv);

        if (rv.getClient() != null && request.dateRendezVous() != null) {
            notificationService.sendNotification(rv.getClient(), "Rendez-vous modifié",
                    "Les informations de votre rendez-vous ont été mises à jour (Date : " + rv.getDateRendezVous() + ").");
        }

        return rv;
    }
}
