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
import sn.oas.facturation.features.auth.data.entity.Agent;
import sn.oas.facturation.features.auth.data.entity.Client;
import sn.oas.facturation.features.auth.service.AuthService;
import sn.oas.facturation.features.auth.service.UserService;
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
import sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelier;

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
        Agent agent = authService.getAgentConnecte();
        Client client = userService.getClientById(request.clientId());
        Vehicule vehicule = getVehicule(request.vehiculeId());

        if (!vehicule.getClient().getId().equals(client.getId())) {
            throw new IllegalArgumentException("Le véhicule ne correspond pas au client");
        }
        
        FicheAtelier ficheAtelier = null;
        if (request.ficheAtelierId() != null) {
            ficheAtelier = ficheAtelierRepository.findById(request.ficheAtelierId())
                .orElseThrow(() -> new IllegalArgumentException("Fiche atelier introuvable"));
        }

        DevisPrevisionnel devis = DevisPrevisionnel.builder()
                .numero(documentNumberGeneratorService.generateNextNumber(sn.oas.facturation.shared.documentNumber.DocumentType.DP))
                .notesReparation(request.notesReparation())
                .montantTotal(request.montantTotal())
                .kilometrageVehicule(request.kilometrageVehicule())
                .vehicule(vehicule)
                .client(client)
                .agent(agent)
                .ficheAtelier(ficheAtelier)
                .build();

        return devisPrevisionnelRepository.save(devis);
    }

    @Transactional
    @Override
    public DevisPrevisionnel modifier(Long id, DevisPrevisionnelRequest request) {
        DevisPrevisionnel devis = getById(id);

        Client client = userService.getClientById(request.clientId());
        Vehicule vehicule = getVehicule(request.vehiculeId());

        if (!vehicule.getClient().getId().equals(client.getId())) {
            throw new IllegalArgumentException("Le véhicule ne correspond pas au client");
        }

        devis.setNotesReparation(request.notesReparation());
        devis.setMontantTotal(request.montantTotal());
        devis.setKilometrageVehicule(request.kilometrageVehicule());
        devis.setVehicule(vehicule);
        devis.setClient(client);

        return devisPrevisionnelRepository.save(devis);
    }

    @Transactional
    @Override
    public void supprimer(Long id) {
        if (!devisPrevisionnelRepository.existsById(id)) {
            throw new RuntimeException("Devis prévisionnel introuvable avec l'id : " + id);
        }
        devisPrevisionnelRepository.deleteById(id);
    }

    @Override
    public DevisPrevisionnel getById(Long id) {
        return devisPrevisionnelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Devis prévisionnel introuvable avec l'id : " + id));
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
