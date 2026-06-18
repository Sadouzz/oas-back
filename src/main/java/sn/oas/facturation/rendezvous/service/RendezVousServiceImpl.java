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
import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.ficheAtelier.repository.FicheAtelierRepository;
import sn.oas.facturation.ficheAtelier.data.enums.StatutReparation;
import sn.oas.facturation.mecanicien.data.entity.Mecanicien;
import sn.oas.facturation.mecanicien.repository.MecanicienRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RendezVousServiceImpl implements RendezVousService {

    private final RendezVousRepository rendezvousRepository;
    private final VehiculeRepository vehiculeRepository;
    private final NotificationService notificationService;
    private final FicheAtelierRepository ficheAtelierRepository;
    private final MecanicienRepository mecanicienRepository;

    @Transactional
    @Override
    public RendezVousResponse bookRendezVous(Client client, RendezVousRequest request) {
        Vehicule vehicule = null;
        if (request.vehiculeId() != null) {
            vehicule = vehiculeRepository.findById(request.vehiculeId())
                    .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));
            if (!vehicule.getClient().getId().equals(client.getId())) {
                throw new IllegalArgumentException("Le véhicule n'appartient pas au client connecté");
            }
        }

        RendezVous rv = RendezVous.builder()
                .client(client)
                .vehicule(vehicule)
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

    @Override
    public List<RendezVousResponse> getClientRendezVous(Client client) {
        return rendezvousRepository.findByClientIdOrderByDateCreationDesc(client.getId())
                .stream()
                .map(RendezVousResponse::of)
                .collect(Collectors.toList());
    }

    @Override
    public List<RendezVousResponse> getClientRendezVousByStatus(Client client, RendezVousStatus status) {
        return rendezvousRepository.findByClientIdAndStatutOrderByDateCreationDesc(client.getId(), status)
                .stream()
                .map(RendezVousResponse::of)
                .collect(Collectors.toList());
    }

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
            throw new IllegalStateException("Le rendez-vous doit être associé à un véhicule pour être validé et transformé en fiche d'atelier.");
        }

        if (mecanicienIds == null || mecanicienIds.isEmpty()) {
            throw new IllegalArgumentException("Au moins un mécanicien doit être assigné pour valider le rendez-vous");
        }

        // 1. Changer le statut du rendez-vous à CONFIRME
        rv.setStatut(RendezVousStatus.CONFIRME);
        rendezvousRepository.save(rv);

        // 2. Récupérer les mécaniciens
        List<Mecanicien> mecaniciens = mecanicienRepository.findAllById(mecanicienIds);
        if (mecaniciens.isEmpty()) {
            throw new IllegalArgumentException("Aucun mécanicien valide trouvé pour les IDs fournis");
        }

        // 3. Créer la fiche atelier
        FicheAtelier ficheAtelier = FicheAtelier.builder()
                .numero("FA-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .descriptionTravaux(rv.getMotif())
                .listeReception("")
                .listeDefauts("")
                .vehicule(rv.getVehicule())
                .mecaniciens(mecaniciens)
                .statut(StatutReparation.A_FAIRE)
                .build();

        ficheAtelierRepository.save(ficheAtelier);

        // 4. Notification au client
        String titre = "Rendez-vous validé";
        String message = "Votre rendez-vous du " + rv.getDateRendezVous() + " a été validé. Une fiche d'atelier " + ficheAtelier.getNumero() + " a été créée.";
        notificationService.sendNotification(rv.getClient(), titre, message);

        return RendezVousResponse.of(rv);
    }
}
