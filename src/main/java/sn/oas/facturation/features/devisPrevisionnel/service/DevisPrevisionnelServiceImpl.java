package sn.oas.facturation.features.devisPrevisionnel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final HtmlToPdfService htmlToPdfService;

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
        String html = construireHtmlDevis(devis);
        return htmlToPdfService.genererHtmlEnPdf(html);
    }

    // -------------------------------------------------------------------------
    // Construction du HTML reproduisant exactement pdf_dp.php
    // -------------------------------------------------------------------------
    private String construireHtmlDevis(DevisPrevisionnel devis) {
        // --- Données ---
        String numero       = devis.getNumero() != null ? devis.getNumero() : String.valueOf(devis.getId());
        String agentNom     = devis.getAgent() != null
                ? devis.getAgent().getFirstName() + " " + devis.getAgent().getLastName()
                : "";
        String dateDevis    = devis.getDateCreation() != null
                ? devis.getDateCreation().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))
                : "";

        // Client
        String clientNum    = devis.getClient() != null ? String.valueOf(devis.getClient().getId()) : "";
        String clientNom    = devis.getClient() != null
                ? devis.getClient().getFirstName() + " " + devis.getClient().getLastName()
                : "";
        String clientTel    = devis.getClient() != null ? safe(devis.getClient().getPhone()) : "";
        String clientEmail  = devis.getClient() != null ? safe(devis.getClient().getEmail()) : "";
        String clientAdresse= devis.getClient() != null ? safe(devis.getClient().getAdresse()) : "";

        // Véhicule
        String annee        = devis.getVehicule() != null && devis.getVehicule().getAnnee() != null
                ? String.valueOf(devis.getVehicule().getAnnee()) : "";
        String marque       = devis.getVehicule() != null ? safe(devis.getVehicule().getMarque()) : "";
        String modele       = devis.getVehicule() != null ? safe(devis.getVehicule().getModele()) : "";
        String immat        = devis.getVehicule() != null ? safe(devis.getVehicule().getImmatriculation()) : "";
        String km           = devis.getKilometrageVehicule() != null
                ? String.valueOf(devis.getKilometrageVehicule().longValue()) : "";
        String chassis      = devis.getVehicule() != null ? safe(devis.getVehicule().getNumeroChassis()) : "";

        // Montant formaté  ex: 20.000
        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.FRENCH);
        sym.setGroupingSeparator('.');
        sym.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("#,##0", sym);
        String montantFormate = devis.getMontantTotal() != null
                ? df.format(devis.getMontantTotal()) : "0";

        // Réparations – on convertit les sauts de ligne en <br/>
        String reparations = "";
        if (devis.getNotesReparation() != null) {
            reparations = devis.getNotesReparation()
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\r\n", "<br/>")
                    .replace("\n", "<br/>");
        }

        // --- HTML ---
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
            + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
            + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"fr\">\n"
            + "<head>\n"
            + "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n"
            + "  <title>Devis Pr&#233;visionnel " + numero + "</title>\n"
            + "  <style type=\"text/css\">\n"
            + "    body { font-family: Helvetica, Arial, sans-serif; font-size: 13px; margin: 20px; }\n"
            + "    table { background: white; width: 100%; border-collapse: collapse; }\n"
            + "    .header-table th { vertical-align: top; padding: 4px 8px; font-weight: normal; }\n"
            + "    .header-table th.left  { width: 38%; text-align: left; }\n"
            + "    .header-table th.mid   { width: 20%; text-align: left; }\n"
            + "    .header-table th.right { width: 42%; text-align: left; }\n"
            + "    .title-dp { font-size: 14px; font-weight: bold; }\n"
            + "    .vehicle-table { font-size: 12px; border: 1px solid #000; }\n"
            + "    .vehicle-table th, .vehicle-table td { border: 1px solid #000; padding: 4px 6px; text-align: center; }\n"
            + "    .vehicle-table thead tr { background-color: #f5f5f0; }\n"
            + "    .montant { font-size: 18px; font-weight: bold; }\n"
            + "    .remarques-label { font-size: 15px; font-weight: bold; }\n"
            + "    .remarques-value { font-size: 17px; font-weight: bold; }\n"
            + "    .repa-label { font-size: 15px; font-weight: bold; }\n"
            + "    .repa-text  { font-size: 17px; font-weight: bold; }\n"
            + "    .signature-table { width: 100%; margin-top: 60px; }\n"
            + "    .signature-table td { width: 50%; vertical-align: top; font-size: 13px; }\n"
            + "    u { text-decoration: underline; }\n"
            + "  </style>\n"
            + "</head>\n"
            + "<body>\n"

            // ── En-tête : 3 colonnes ──────────────────────────────────────────
            + "<table class=\"header-table\">\n"
            + "  <tr>\n"
            + "    <th class=\"left\">\n"
            + "      <span class=\"title-dp\"><u>DEVIS PREVISIONNEL :</u></span><br/>\n"
            + "      Num. DK/" + numero + "<br/><br/>\n"
            + "    </th>\n"
            + "    <th class=\"mid\">\n"
            + "      Agent : " + agentNom + "<br/><br/><br/><br/><br/>\n"
            + "    </th>\n"
            + "    <th class=\"right\">\n"
            + "      Dakar le " + dateDevis + "<br/><br/>\n"
            + "      <u>CLIENT</u> Num. " + clientNum + "<br/>\n"
            + "      Nom : " + clientNom + "<br/>\n"
            + "      T&#233;l : " + clientTel + "<br/>\n"
            + "      Email : " + clientEmail + "<br/>\n"
            + "      Adresse : " + clientAdresse + "<br/>\n"
            + "    </th>\n"
            + "  </tr>\n"
            + "</table>\n"

            + "<br/>\n"

            // ── Tableau véhicule ─────────────────────────────────────────────
            + "<table class=\"vehicle-table\">\n"
            + "  <thead>\n"
            + "    <tr>\n"
            + "      <th style=\"width:14%\">Ann&#233;e</th>\n"
            + "      <th style=\"width:16%\">Marque</th>\n"
            + "      <th style=\"width:16%\">Mod&#232;le</th>\n"
            + "      <th style=\"width:17%\">No. immatriculation</th>\n"
            + "      <th style=\"width:11%\">Kilom&#233;trage</th>\n"
            + "      <th style=\"width:26%\">No. chassie</th>\n"
            + "    </tr>\n"
            + "  </thead>\n"
            + "  <tbody>\n"
            + "    <tr>\n"
            + "      <td>" + annee + "</td>\n"
            + "      <td>" + marque + "</td>\n"
            + "      <td>" + modele + "</td>\n"
            + "      <td>" + immat + "</td>\n"
            + "      <td>" + km + "</td>\n"
            + "      <td>" + chassis + "</td>\n"
            + "    </tr>\n"
            + "  </tbody>\n"
            + "</table>\n"

            + "<br/>\n"

            // ── Corps ────────────────────────────────────────────────────────
            + "<p>\n"
            + "Cher client,<br/>\n"
            + "Suivant un premier diagnostic op&#233;r&#233; par la Soci&#233;t&#233; OAS, le montant <br/>\n"
            + "des r&#233;parations sera compris dans une fourchette allant de:\n"
            + " <span class=\"montant\">" + montantFormate + " Frs CFA HT.</span>\n"
            + "</p>\n"

            + "<br/>\n"

            + "<p><span class=\"remarques-label\"><u>Remarques :</u></span>&#160;"
            + "<span class=\"remarques-value\">Sous r&#233;serve de vises cach&#233;s</span></p>\n"

            + "<br/>\n"

            // ── Réparations ──────────────────────────────────────────────────
            + "<p><span class=\"repa-label\"><u>R&#233;parations :</u></span><br/>\n"
            + "<span class=\"repa-text\">" + reparations + "</span></p>\n"

            + "<br/><br/>\n"

            // ── Signatures ───────────────────────────────────────────────────
            + "<table class=\"signature-table\">\n"
            + "  <tr>\n"
            + "    <td>\n"
            + "      SIGNATURE DU CLIENT <br/>\n"
            + "      pr&#233;c&#233;d&#233;e de la mention <br/>\n"
            + "      LU &amp; APPROUVE\n"
            + "    </td>\n"
            + "    <td>\n"
            + "      VISA DU RECEPTIONNISTE:\n"
            + "    </td>\n"
            + "  </tr>\n"
            + "</table>\n"

            + "</body>\n"
            + "</html>\n";
    }

    /** Retourne une chaîne vide si la valeur est null. */
    private String safe(String val) {
        return val != null ? val : "";
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
