package sn.oas.facturation.features.devisPrevisionnel.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.oas.facturation.features.auth.service.AuthService;
import sn.oas.facturation.features.user.service.UserService;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.devisPrevisionnel.data.entity.DevisPrevisionnel;
import sn.oas.facturation.features.devisPrevisionnel.dto.DevisPrevisionnelRequest;
import sn.oas.facturation.features.devisPrevisionnel.repository.DevisPrevisionnelRepository;
import sn.oas.facturation.features.facturation.data.enums.StatutFacturation;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.vehicule.service.VehiculeService;

import java.io.ByteArrayOutputStream;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sn.oas.facturation.features.ficheAtelier.repository.FicheAtelierRepository;
import sn.oas.facturation.features.user.data.entity.Agent;
import sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.shared.exception.BadRequestException;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class DevisPrevisionnelServiceImpl implements DevisPrevisionnelService {

    private final DevisPrevisionnelRepository devisPrevisionnelRepository;
    private final FicheAtelierRepository ficheAtelierRepository;
    private final VehiculeService vehiculeService;
    private final AuthService authService;
    private final UserService userService;
    private final sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService documentNumberGeneratorService;

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

        Client client = null;
        if (request.clientId() != null) {
            client = userService.getClientById(request.clientId());
        } else if (ficheAtelier != null && ficheAtelier.getClient() != null) {
            client = ficheAtelier.getClient();
        }

        if (client == null) {
            throw new BadRequestException("Le client est obligatoire pour créer un devis prévisionnel");
        }

        Vehicule vehicule = null;
        if (request.vehiculeId() != null) {
            vehicule = getVehicule(request.vehiculeId());
        } else if (ficheAtelier != null && ficheAtelier.getVehicule() != null) {
            vehicule = ficheAtelier.getVehicule();
        }

        if (vehicule == null) {
            throw new BadRequestException("Le véhicule est obligatoire pour créer un devis prévisionnel");
        }

        if (vehicule.getClient() != null && !vehicule.getClient().getId().equals(client.getId())) {
            throw new BadRequestException("Le véhicule ne correspond pas au client");
        }

        Garage garage = (agent != null && agent.getGarage() != null)
                ? agent.getGarage()
                : (ficheAtelier != null && ficheAtelier.getGarage() != null ? ficheAtelier.getGarage() : documentNumberGeneratorService.getCurrentGarage());

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
                .build();

        DevisPrevisionnel saved = devisPrevisionnelRepository.save(devis);
        if (ficheAtelier != null) {
            ficheAtelier.setDevisPrevisionnel(saved);
            ficheAtelierRepository.save(ficheAtelier);
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

        if (vehicule.getClient() != null && !vehicule.getClient().getId().equals(client.getId())) {
            throw new BadRequestException("Le véhicule ne correspond pas au client");
        }

        if (request.notesReparation() != null) devis.setNotesReparation(request.notesReparation());
        if (request.montantTotal() != null) devis.setMontantTotal(request.montantTotal());
        if (request.kilometrageVehicule() != null) devis.setKilometrageVehicule(request.kilometrageVehicule());
        devis.setVehicule(vehicule);
        devis.setClient(client);

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
    public org.springframework.data.domain.Page<DevisPrevisionnel> getAll(int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return devisPrevisionnelRepository.findAll(pageable);
    }

    @Override
    public List<DevisPrevisionnel> getAll() {
        return devisPrevisionnelRepository.findAll();
    }

    @Override
    public List<DevisPrevisionnel> getByClient(Long clientId) {
        return devisPrevisionnelRepository.findByClientId(clientId);
    }

    @Override
    public List<DevisPrevisionnel> getByVehicule(Long vehiculeId) {
        return devisPrevisionnelRepository.findByVehiculeId(vehiculeId);
    }

    public java.util.Optional<DevisPrevisionnel> getByFicheAtelierId(Long ficheAtelierId) {
        return devisPrevisionnelRepository.findByFicheAtelierId(ficheAtelierId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DevisPrevisionnel> getClientDevis(Client client) {
        return devisPrevisionnelRepository.findByClientIdOrderByDateCreationDesc(client.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DevisPrevisionnel> search(String keyword) {
        return devisPrevisionnelRepository.searchDevis(keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DevisPrevisionnel> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by("id").descending());
        return devisPrevisionnelRepository.searchDevis(keyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(Long id) {
        DevisPrevisionnel devis = getById(id);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph title = new Paragraph("Devis prévisionnel", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" ", normalFont));
            document.add(new Paragraph("Numéro : " + devis.getId(), normalFont));
            document.add(new Paragraph("Client : " + devis.getClient().getFirstName() + " " + devis.getClient().getLastName(), normalFont));
            document.add(new Paragraph("Véhicule : " + devis.getVehicule().getImmatriculation(), normalFont));
            document.add(new Paragraph("Montant total : " + devis.getMontantTotal(), normalFont));
            document.add(new Paragraph("Statut : " + devis.getStatut(), normalFont));
            document.add(new Paragraph("Notes : " + (devis.getNotesReparation() != null ? devis.getNotesReparation() : ""), normalFont));
        } catch (DocumentException e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        } finally {
            document.close();
        }

        return outputStream.toByteArray();
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
        if (!devis.getClient().getId().equals(client.getId())) {
            throw new IllegalArgumentException("Accès non autorisé à ce devis");
        }
        devis.setStatut(StatutFacturation.ACCEPTE);
        return devisPrevisionnelRepository.save(devis);
    }

    @Override
    @Transactional
    public DevisPrevisionnel clientRefuser(Client client, Long id) {
        DevisPrevisionnel devis = getById(id);
        if (!devis.getClient().getId().equals(client.getId())) {
            throw new IllegalArgumentException("Accès non autorisé à ce devis");
        }
        devis.setStatut(StatutFacturation.REJETE);
        return devisPrevisionnelRepository.save(devis);
    }

    private Vehicule getVehicule(Long vehiculeId) {
        return vehiculeService.getVehiculeById(vehiculeId);
    }

}
