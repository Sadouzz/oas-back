package sn.oas.facturation.features.ficheAtelier.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.features.ficheAtelier.data.entity.LigneDefaut;
import sn.oas.facturation.features.ficheAtelier.data.entity.LigneReception;
import sn.oas.facturation.features.ficheAtelier.repository.FicheAtelierRepository;
import sn.oas.facturation.features.ficheAtelier.repository.FicheAtelierPdfRepository;
import sn.oas.facturation.features.pdfGenerator.service.HtmlToPdfService;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class FicheAtelierPdfService {
    private final FicheAtelierRepository repository;
    private final FicheAtelierPdfRepository pdfRepository;
    private final HtmlToPdfService pdfService;

    @Transactional(readOnly = true)
    public byte[] generer(Long id) {
        FicheAtelier fiche = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fiche atelier introuvable"));
        var snapshot = pdfRepository.findById(id);
        if (snapshot.isPresent()) return snapshot.get().getContenu();
        if (fiche.getSignatureBase64() == null || fiche.getSignatureReceptionnaireBase64() == null) {
            throw new IllegalStateException("La fiche doit porter les deux signatures d'entrée.");
        }
        StringBuilder html = new StringBuilder("""
                <html><head><meta charset="UTF-8"/><style>
                body{font-family:Arial,sans-serif;font-size:10pt;color:#1c2b39}
                h1{font-size:18pt}h2{font-size:12pt;margin-top:16px}
                table{width:100%;border-collapse:collapse;margin:8px 0}
                td,th{border:1px solid #aab4bc;padding:5px;text-align:left}
                .signatures td{width:50%;height:95px;vertical-align:top}
                img{max-width:220px;max-height:75px}
                </style></head><body><h1>Fiche Atelier</h1>
                """);
        html.append("<p><b>N° </b>").append(escape(fiche.getNumero())).append(" — ")
                .append(fiche.getCreatedAt() == null ? "" : fiche.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .append("</p><table>");
        row(html, "Client", fiche.getClient() == null ? "" : fiche.getClient().getFirstName() + " " + fiche.getClient().getLastName());
        row(html, "Véhicule", fiche.getVehicule() == null ? "" : fiche.getVehicule().getImmatriculation());
        row(html, "Chauffeur", fiche.getNomChauffeur());
        row(html, "Téléphone chauffeur", fiche.getTelephoneChauffeur());
        row(html, "Kilométrage", fiche.getKilometrage() == null ? "" : fiche.getKilometrage().toString());
        row(html, "Niveau de carburant", fiche.getNiveauEssence());
        row(html, "Travaux demandés", fiche.getDesignationTravaux());
        row(html, "Remarques", fiche.getNb());
        html.append("</table><h2>État de réception</h2><table><tr><th>Élément</th><th>État</th></tr>");
        if (fiche.getLignesReception() != null) for (LigneReception ligne : fiche.getLignesReception()) {
            html.append("<tr><td>").append(escape(ligne.getNom())).append("</td><td>")
                    .append(ligne.getEtat() == null ? "Non renseigné" : ligne.getEtat() ? "Oui" : "Non")
                    .append("</td></tr>");
        }
        html.append("</table><h2>Défauts constatés</h2><table><tr><th>Élément</th><th>Détail</th></tr>");
        if (fiche.getLignesDefauts() != null) for (LigneDefaut ligne : fiche.getLignesDefauts()) {
            html.append("<tr><td>").append(escape(ligne.getNom())).append("</td><td>")
                    .append(escape(ligne.getDesignation())).append("</td></tr>");
        }
        html.append("</table>").append(CONDITIONS);
        html.append("<h2>Signatures à la réception</h2><table class='signatures'><tr><td>Réceptionnaire<br/>")
                .append(image(fiche.getSignatureReceptionnaireBase64()))
                .append("</td><td>Client<br/>").append(image(fiche.getSignatureBase64()))
                .append("</td></tr></table></body></html>");
        return pdfService.genererHtmlEnPdf(html.toString());
    }

    private static void row(StringBuilder html, String name, String value) {
        html.append("<tr><th>").append(escape(name)).append("</th><td>").append(escape(value)).append("</td></tr>");
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String image(String value) {
        if (value == null || !value.matches("^data:image/png;base64,[A-Za-z0-9+/=]+$")) return "Signature indisponible";
        return "<img src='" + value + "' alt='Signature'/><br/>";
    }

    private static final String CONDITIONS = """
            <h2>Conditions générales de réparation</h2>
            <h3>Article 1er : Engagement juridique des parties</h3>
            <p>Le présent contrat constitue dès sa signature un engagement, tant pour le réparateur que pour le client.</p>
            <p>Pour sa part, la Société OAS s'engage à respecter l'ordre donné.</p>
            <p>Dans le cas où celui-ci ne pourrait être tenu, soit par défaut d'approvisionnement, soit par cas de force majeure, le réparateur devra en informer son client et lui donner les motifs du retard de la livraison.</p>
            <p>Le client, en ce qui le concerne, s'engage à s'acquitter, à la livraison du véhicule, du montant de la facture résultant des travaux commandés, sauf accord contraire expressément passé lors de l'établissement de l'ordre de réparation.</p>
            <h3>Article 2 : Modifications éventuelles des travaux prévus par l'ordre de réparation</h3>
            <p>Le professionnel, pour satisfaire à l'obligation de résultat à laquelle il est légalement tenu pourra être amené au cours de la réparation, à constater la nécessité d'effectuer des travaux complémentaires non prévus sur l'ordre de réparation.</p>
            <p>Si ces travaux entraînent une facturation dont le montant excède plus de 10% de l'estimation prévue, le réparateur devra en informer son client et obtenir son accord sur ce nouveau montant.</p>
            <h3>Article 3 : Restitution des pièces changées</h3>
            <p>Les pièces usagées restent la propriété du client, elles lui sont remises ou présentées au moment de la restitution de son véhicule.</p>
            <p>Si les pièces usagées ne sont pas reprises par le client, au moment de la restitution du véhicule, le réparateur peut en disposer librement.</p>
            <p>Font exception au principe de la restitution par le réparateur les pièces changées dans le cadre de la garantie contractuelle et de l'échange standard.</p>
            <h3>Article 4 : Limites de responsabilités</h3>
            <p>La Société OAS ne répond que de ses fautes professionnelles dans l'exécution de son travail, et uniquement pour les travaux commandés par le client sur son véhicule.</p>
            <p>La Société OAS n'est responsable desdites fautes professionnelles qu'envers la personne qui lui a confié le véhicule et qui a signé l'ordre de réparation.</p>
            <p>La Société OAS n'est pas responsable des produits endommagés ou cassés par négligence ou mauvaise manutention du fait d'un autre réparateur.</p>
            <p>La Société OAS n'est également pas responsable des dommages causés du fait des agissements personnels du client, ou pour un cas de force majeure.</p>
            <p>La Société OAS n'est pas responsable des objets laissés dans le véhicule par le client.</p>
            <h3>Article 5 : Contestations</h3>
            <p>En cas de différend relatif à l'exécution de l'ordre de réparation, il est fait attribution de juridiction au Tribunal du lieu de la signature du présent contrat.</p>
            """;
}
