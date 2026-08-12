package sn.oas.facturation.rendezvous.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.notification.service.NotificationService;
import sn.oas.facturation.rendezvous.data.entity.RendezVous;
import sn.oas.facturation.rendezvous.data.enums.RendezVousStatus;
import sn.oas.facturation.rendezvous.dto.RendezVousRequest;
import sn.oas.facturation.rendezvous.dto.RendezVousResponse;
import sn.oas.facturation.rendezvous.repository.RendezVousRepository;
import sn.oas.facturation.vehicule.data.entity.Vehicule;
import sn.oas.facturation.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.garage.data.entity.Garage;
import sn.oas.facturation.garage.repository.GarageRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RendezVousServiceImpl implements RendezVousService {

    private final RendezVousRepository rendezvousRepository;
    private final VehiculeRepository vehiculeRepository;
    private final GarageRepository garageRepository;
    private final NotificationService notificationService;
    private final sn.oas.facturation.ordreReparation.service.OrdreReparationService ordreReparationService;
    private final sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService documentNumberGeneratorService;

    @Transactional
    @Override
    public RendezVousResponse bookRendezVous(Client client, RendezVousRequest request) {
        if (request.vehiculeId() == null) {
            throw new IllegalArgumentException("Veuillez sélectionner un véhicule");
        }
        
        if (request.dateRendezVous() != null && request.dateRendezVous().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("La date du rendez-vous ne peut pas être dans le passé");
        }
        
        Vehicule vehicule = vehiculeRepository.findById(request.vehiculeId())
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));
        if (!vehicule.getClient().getId().equals(client.getId())) {
            throw new IllegalArgumentException("Le véhicule n'appartient pas au client connecté");
        }
        if (rendezvousRepository.existsByVehiculeIdAndStatut(vehicule.getId(), RendezVousStatus.EN_ATTENTE)) {
            throw new IllegalArgumentException("Ce véhicule a déjà un rendez-vous en attente");
        }
        
        if (rendezvousRepository.existsByVehiculeIdAndStatutAndDateRendezVousAfter(vehicule.getId(), RendezVousStatus.CONFIRME, java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Ce véhicule a déjà un rendez-vous confirmé à venir");
        }

        if (ordreReparationService.existsByVehiculeIdAndStatutNotIn(vehicule.getId(), 
                java.util.List.of(sn.oas.facturation.ordreReparation.data.enums.StatutOrdreReparation.TERMINE, 
                                  sn.oas.facturation.ordreReparation.data.enums.StatutOrdreReparation.LIVRE))) {
            throw new IllegalArgumentException("Ce véhicule est actuellement en réparation");
        }

        Garage garage = null;
        if (request.garageId() != null) {
            garage = garageRepository.findById(request.garageId())
                    .orElseThrow(() -> new RuntimeException("Garage non trouvé"));
        } else {
            throw new IllegalArgumentException("Veuillez sélectionner un garage");
        }

        RendezVous rv = RendezVous.builder()
                .numero(documentNumberGeneratorService.generateNextNumber(sn.oas.facturation.shared.documentNumber.DocumentType.RDV))
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

        return RendezVousResponse.of(rv);
    }

    @Transactional
    @Override
    public RendezVousResponse cancelRendezVous(Client client, Long id) {
        RendezVous rv = rendezvousRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé"));
        if (!rv.getClient().getId().equals(client.getId())) {
            throw new IllegalArgumentException("Accès non autorisé à ce rendez-vous");
        }
        rv.setStatut(RendezVousStatus.ANNULE);
        rendezvousRepository.save(rv);

        notificationService.sendNotification(client, "Rendez-vous annulé", 
                "Vous avez annulé votre rendez-vous du " + rv.getDateRendezVous());

        return RendezVousResponse.of(rv);
    }

    @Transactional(readOnly = true)
    @Override
    public List<RendezVousResponse> getClientRendezVous(Client client) {
        return rendezvousRepository.findByClientIdOrderByDateCreationDesc(client.getId())
                .stream()
                .map(RendezVousResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<RendezVousResponse> getClientRendezVousByStatus(Client client, RendezVousStatus status) {
        return rendezvousRepository.findByClientIdAndStatutOrderByDateCreationDesc(client.getId(), status)
                .stream()
                .map(RendezVousResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<RendezVousResponse> getAllRendezVous() {
        return rendezvousRepository.findAllByOrderByDateCreationDesc()
                .stream()
                .map(RendezVousResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public RendezVousResponse updateRendezVousStatus(Long id, RendezVousStatus status, String commentaire) {
        RendezVous rv = rendezvousRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé"));
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

        return RendezVousResponse.of(rv);
    }

    @Transactional
    @Override
    public RendezVousResponse validerRendezVous(Long id, List<Long> mecanicienIds) {
        RendezVous rv = rendezvousRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé"));
        if (rv.getVehicule() == null) {
            throw new RuntimeException("Impossible de valider un rendez-vous sans véhicule associé.");
        }
        
        rv.setStatut(RendezVousStatus.CONFIRME);
        rendezvousRepository.save(rv);

        sn.oas.facturation.ordreReparation.dto.OrdreReparationRequest faReq = new sn.oas.facturation.ordreReparation.dto.OrdreReparationRequest();
        faReq.setVehiculeId(rv.getVehicule().getId());
        faReq.setDescriptionTravaux(rv.getMotif());
        faReq.setStatut(sn.oas.facturation.ordreReparation.data.enums.StatutOrdreReparation.A_FAIRE);
        
        sn.oas.facturation.ordreReparation.data.entity.OrdreReparation fiche = ordreReparationService.createOrdreReparation(faReq);
        
        if (mecanicienIds != null) {
            for (Long mId : mecanicienIds) {
                ordreReparationService.assignMecanicien(fiche.getId(), mId);
            }
        }

        notificationService.sendNotification(rv.getClient(), "Rendez-vous validé", 
                "Votre rendez-vous du " + rv.getDateRendezVous() + " a été validé et une fiche atelier a été créée.");

        return RendezVousResponse.of(rv);
    }
    @Transactional
    @Override
    public RendezVousResponse updateDate(Long id, java.time.LocalDateTime nouvelleDate) {
        RendezVous rv = rendezvousRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé"));
        if (nouvelleDate.isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("La nouvelle date ne peut pas être dans le passé");
        }
        rv.setDateRendezVous(nouvelleDate);
        rendezvousRepository.save(rv);

        notificationService.sendNotification(rv.getClient(), "Date de rendez-vous modifiée",
                "La date de votre rendez-vous a été modifiée au " + nouvelleDate + ".");

        return RendezVousResponse.of(rv);
    }
}
