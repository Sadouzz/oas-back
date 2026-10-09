package sn.oas.facturation.features.devisPrevisionnel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.oas.facturation.features.pdfGenerator.service.DevisPrevisionnelGenerator;
import sn.oas.facturation.features.pdfGenerator.service.HtmlToPdfService;

import sn.oas.facturation.features.auth.service.AuthService;
import sn.oas.facturation.features.user.service.UserService;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.devisPrevisionnel.data.entity.DevisPrevisionnel;
import sn.oas.facturation.features.devisPrevisionnel.dto.DevisPrevisionnelRequest;
import sn.oas.facturation.features.devisPrevisionnel.repository.DevisPrevisionnelRepository;
import sn.oas.facturation.features.facturation.data.enums.StatutFacturation;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.vehicule.service.VehiculeService;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import sn.oas.facturation.features.ficheAtelier.repository.FicheAtelierRepository;
import sn.oas.facturation.features.user.data.entity.Agent;
import sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService;
import sn.oas.facturation.shared.exception.BadRequestException;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;
import sn.oas.facturation.features.notification.service.EmailService;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;

@Service
@RequiredArgsConstructor
public class DevisPrevisionnelServiceImpl implements DevisPrevisionnelService {

    private final DevisPrevisionnelRepository devisPrevisionnelRepository;
    private final FicheAtelierRepository ficheAtelierRepository;
    private final OrdreReparationRepository ordreReparationRepository;
    private final VehiculeService vehiculeService;
    private final AuthService authService;
    private final UserService userService;
    private final DocumentNumberGeneratorService documentNumberGeneratorService;
    private final EmailService emailService;
    private final DevisPrevisionnelGenerator devisPrevisionnelGenerator;

    @Transactional
    @Override
    public DevisPrevisionnel creer(DevisPrevisionnelRequest request) {
        if (request == null) {
            throw new BadRequestException("Les données du devis sont obligatoires");
        }

        Agent agent = authService.getAgentConnecte();

        FicheAtelier ficheAtelier = null;
        if (request.ficheAtelierId() != null) {
            ficheAtelier = ficheAtelierRepository.findById(request.ficheAtelierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fiche atelier introuvable avec l'id : " + request.ficheAtelierId()));
        }

        OrdreReparation ordreReparation = null;
        if (request.ordreReparationId() != null) {
            ordreReparation = ordreReparationRepository.findById(request.ordreReparationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ordre de réparation introuvable avec l'id : " + request.ordreReparationId()));
            if (ficheAtelier == null && ordreReparation.getFicheAtelier() != null) {
                ficheAtelier = ordreReparation.getFicheAtelier();
            }
        }

        Client client = null;
        if (request.clientId() != null) {
            client = userService.getClientById(request.clientId());
        } else if (ficheAtelier != null && ficheAtelier.getClient() != null) {
            client = ficheAtelier.getClient();
        } else if (ordreReparation != null && ordreReparation.getClient() != null) {
            client = ordreReparation.getClient();
        } else if (ordreReparation != null && ordreReparation.getVehicule() != null && ordreReparation.getVehicule().getClient() != null) {
            client = ordreReparation.getVehicule().getClient();
        }

        if (client == null) {
            throw new BadRequestException("Le client est obligatoire pour créer un devis prévisionnel");
        }

        Vehicule vehicule = null;
        if (request.vehiculeId() != null) {
            vehicule = getVehicule(request.vehiculeId());
        } else if (ficheAtelier != null && ficheAtelier.getVehicule() != null) {
            vehicule = ficheAtelier.getVehicule();
        } else if (ordreReparation != null && ordreReparation.getVehicule() != null) {
            vehicule = ordreReparation.getVehicule();
        }

        if (vehicule == null) {
            throw new BadRequestException("Le véhicule est obligatoire pour créer un devis prévisionnel");
        }
        sn.oas.facturation.features.vehicule.service.VehiculeActivationPolicy.requireActive(vehicule);

        if (vehicule.getClient() != null && !vehicule.getClient().getId().equals(client.getId())) {
            throw new BadRequestException("Le véhicule ne correspond pas au client");
        }

        Garage garage = (agent != null && agent.getGarage() != null)
                ? agent.getGarage()
                : (ficheAtelier != null && ficheAtelier.getGarage() != null
                    ? ficheAtelier.getGarage()
                    : (ordreReparation != null && ordreReparation.getGarage() != null
                        ? ordreReparation.getGarage()
                        : documentNumberGeneratorService.getCurrentGarage()));

        Double kilometrage = request.kilometrageVehicule();
        if (kilometrage == null && ficheAtelier != null && ficheAtelier.getKilometrage() != null) {
            kilometrage = ficheAtelier.getKilometrage().doubleValue();
        }
        if (kilometrage == null) {
            kilometrage = 0.0;
        }

        java.math.BigDecimal montant = request.montantTotal() != null ? request.montantTotal() : java.math.BigDecimal.ZERO;

        DevisPrevisionnel devis = DevisPrevisionnel.builder()
                .numero(documentNumberGeneratorService.generateNextNumber(garage, sn.oas.facturation.shared.documentNumber.DocumentType.DP))
                .notesReparation(request.notesReparation())
                .montantTotal(montant)
                .kilometrageVehicule(kilometrage)
                .vehicule(vehicule)
                .client(client)
                .agent(agent)
                .garage(garage)
                .ficheAtelier(ficheAtelier)
                .ordreReparation(ordreReparation)
                .build();

        DevisPrevisionnel saved = devisPrevisionnelRepository.save(devis);
        if (ficheAtelier != null) {
            ficheAtelier.setDevisPrevisionnel(saved);
            ficheAtelierRepository.save(ficheAtelier);
        }

        if (client.getEmail() != null && !client.getEmail().isEmpty()) {
            byte[] pdfBytes = generatePdf(saved.getId());
            emailService.sendEmailWithAttachment(
                    client.getEmail(),
                    "Votre devis prévisionnel",
                    "<p>Bonjour,</p><p>Veuillez trouver ci-joint le devis prévisionnel <b>" + saved.getNumero() + "</b> pour votre véhicule " + vehicule.getImmatriculation() + ".</p><p>Cordialement.</p>",
                    "devis_" + saved.getNumero() + ".pdf",
                    pdfBytes,
                    "application/pdf"
            );
        }

        return saved;
    }

    @Transactional
    @Override
    public DevisPrevisionnel modifier(Long id, DevisPrevisionnelRequest request) {
        if (id == null) {
            throw new BadRequestException("L'identifiant du devis est obligatoire");
        }
        DevisPrevisionnel devis = getById(id);

        FicheAtelier ficheAtelier = devis.getFicheAtelier();
        if (request.ficheAtelierId() != null) {
            ficheAtelier = ficheAtelierRepository.findById(request.ficheAtelierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fiche atelier introuvable avec l'id : " + request.ficheAtelierId()));
            devis.setFicheAtelier(ficheAtelier);
        }

        if (request.ordreReparationId() != null) {
            OrdreReparation or = ordreReparationRepository.findById(request.ordreReparationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ordre de réparation introuvable avec l'id : " + request.ordreReparationId()));
            devis.setOrdreReparation(or);
        }

        Client client = null;
        if (request.clientId() != null) {
            client = userService.getClientById(request.clientId());
        } else if (ficheAtelier != null && ficheAtelier.getClient() != null) {
            client = ficheAtelier.getClient();
        } else {
            client = devis.getClient();
        }

        Vehicule vehicule = null;
        if (request.vehiculeId() != null) {
            vehicule = getVehicule(request.vehiculeId());
        } else if (ficheAtelier != null && ficheAtelier.getVehicule() != null) {
            vehicule = ficheAtelier.getVehicule();
        } else {
            vehicule = devis.getVehicule();
        }

        if (client == null) {
            throw new BadRequestException("Le client est obligatoire");
        }
        if (vehicule == null) {
            throw new BadRequestException("Le véhicule est obligatoire");
        }
        sn.oas.facturation.features.vehicule.service.VehiculeActivationPolicy.requireActive(vehicule);

        if (vehicule.getClient() != null && !vehicule.getClient().getId().equals(client.getId())) {
            throw new BadRequestException("Le véhicule ne correspond pas au client");
        }

        if (request.notesReparation() != null) devis.setNotesReparation(request.notesReparation());
        if (request.montantTotal() != null) devis.setMontantTotal(request.montantTotal());
        if (request.kilometrageVehicule() != null) devis.setKilometrageVehicule(request.kilometrageVehicule());
        devis.setVehicule(vehicule);
        devis.setClient(client);
        devis.setUpdatedAt(java.time.LocalDateTime.now());

        return devisPrevisionnelRepository.save(devis);
    }

    @Transactional
    @Override
    public void supprimer(Long id) {
        if (id == null) {
            throw new BadRequestException("L'identifiant du devis est obligatoire");
        }
        DevisPrevisionnel devis = getById(id);
        if (devis.getFicheAtelier() != null) {
            FicheAtelier fa = devis.getFicheAtelier();
            fa.setDevisPrevisionnel(null);
            ficheAtelierRepository.save(fa);
        }
        devisPrevisionnelRepository.delete(devis);
    }

    @Override
    public DevisPrevisionnel getById(Long id) {
        if (id == null) {
            throw new BadRequestException("L'identifiant du devis est obligatoire");
        }
        return devisPrevisionnelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Devis prévisionnel introuvable avec l'id : " + id));
    }

    @Override
    public Page<DevisPrevisionnel> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("updatedAt").nullsLast(),
                Sort.Order.desc("dateCreation").nullsLast(),
                Sort.Order.desc("id")
        ));
        return devisPrevisionnelRepository.findAll(pageable);
    }

    @Override
    public List<DevisPrevisionnel> getAll() {
        return devisPrevisionnelRepository.findAll(Sort.by(
                Sort.Order.desc("updatedAt").nullsLast(),
                Sort.Order.desc("dateCreation").nullsLast(),
                Sort.Order.desc("id")
        ));
    }

    @Override
    public List<DevisPrevisionnel> getByClient(Long clientId) {
        return devisPrevisionnelRepository.findByClientIdOrderByUpdatedAtDesc(clientId);
    }

    @Override
    public List<DevisPrevisionnel> getByVehicule(Long vehiculeId) {
        return devisPrevisionnelRepository.findByVehiculeIdOrderByUpdatedAtDesc(vehiculeId);
    }

    public java.util.Optional<DevisPrevisionnel> getByFicheAtelierId(Long ficheAtelierId) {
        return devisPrevisionnelRepository.findFirstByFicheAtelierIdOrderByUpdatedAtDesc(ficheAtelierId);
    }

    @Override
    public List<DevisPrevisionnel> getListByFicheAtelierId(Long ficheAtelierId) {
        return devisPrevisionnelRepository.findByFicheAtelierIdOrderByUpdatedAtDesc(ficheAtelierId);
    }

    @Override
    public List<DevisPrevisionnel> getByOrdreReparationId(Long ordreReparationId) {
        return devisPrevisionnelRepository.findByOrdreReparationIdOrderByUpdatedAtDesc(ordreReparationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DevisPrevisionnel> getClientDevis(Client client) {
        return devisPrevisionnelRepository.findByClientIdOrderByUpdatedAtDesc(client.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DevisPrevisionnel> search(String keyword) {
        return devisPrevisionnelRepository.searchDevis(keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DevisPrevisionnel> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("updatedAt").nullsLast(),
                Sort.Order.desc("dateCreation").nullsLast(),
                Sort.Order.desc("id")
        ));
        return devisPrevisionnelRepository.searchDevis(keyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(Long id) {
        DevisPrevisionnel devis = getById(id);
        return devisPrevisionnelGenerator.genererDevisPrevisionnelPdf(devis);
    }

    @Override
    @Transactional
    public DevisPrevisionnel valider(Long id) {
        DevisPrevisionnel devis = getById(id);
        devis.setStatut(StatutFacturation.ACCEPTE);
        return devisPrevisionnelRepository.save(devis);
    }

    @Override
    @Transactional
    public DevisPrevisionnel annuler(Long id) {
        DevisPrevisionnel devis = getById(id);
        devis.setStatut(StatutFacturation.ANNULEE);
        return devisPrevisionnelRepository.save(devis);
    }

    @Override
    @Transactional
    public DevisPrevisionnel clientAccepter(Client client, Long id) {
        DevisPrevisionnel devis = getById(id);
        assertClientOwns(client, devis);
        devis.setStatut(StatutFacturation.ACCEPTE);
        return devisPrevisionnelRepository.save(devis);
    }

    @Override
    @Transactional
    public DevisPrevisionnel clientRefuser(Client client, Long id) {
        DevisPrevisionnel devis = getById(id);
        assertClientOwns(client, devis);
        devis.setStatut(StatutFacturation.REJETE);
        return devisPrevisionnelRepository.save(devis);
    }

    private void assertClientOwns(Client client, DevisPrevisionnel devis) {
        if (devis.getClient() == null || client == null || !devis.getClient().getId().equals(client.getId())) {
            throw new sn.oas.facturation.shared.exception.ForbiddenException("Accès non autorisé à ce devis");
        }
    }

    private Vehicule getVehicule(Long vehiculeId) {
        return vehiculeService.getVehiculeById(vehiculeId);
    }

}
