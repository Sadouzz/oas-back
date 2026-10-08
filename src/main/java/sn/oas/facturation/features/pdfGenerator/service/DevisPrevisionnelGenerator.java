package sn.oas.facturation.features.pdfGenerator.service;

import org.springframework.stereotype.Service;
import sn.oas.facturation.features.devisPrevisionnel.data.entity.DevisPrevisionnel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class DevisPrevisionnelGenerator {

    private final HtmlToPdfService htmlToPdfService;

    public DevisPrevisionnelGenerator(HtmlToPdfService htmlToPdfService) {
        this.htmlToPdfService = htmlToPdfService;
    }

    public byte[] genererDevisPrevisionnelPdf(DevisPrevisionnel devis) {
        String html = construireHtmlDevis(devis);
        String numero = devis.getNumero() != null ? devis.getNumero() : String.valueOf(devis.getId());
        String date = devis.getDateCreation() != null ? devis.getDateCreation().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) : "";
        String clientNom = devis.getClient() != null ? devis.getClient().getFirstName() + " " + devis.getClient().getLastName() : "";
        String marque = devis.getVehicule() != null ? safe(devis.getVehicule().getMarque()) : "";
        String modele = devis.getVehicule() != null ? safe(devis.getVehicule().getModele()) : "";
        String immat = devis.getVehicule() != null ? safe(devis.getVehicule().getImmatriculation()) : "";
        String total = devis.getMontantTotal() != null ? devis.getMontantTotal().toPlainString() : "0";
        return htmlToPdfService.genererHtmlEnPdf(html, "DEVIS_PREVISIONNEL", java.util.Map.of(
                "numero", numero, "date", date, "clientNom", clientNom, "marque", marque, "modele", modele,
                "immatriculation", immat, "montantTotal", total,
                "reparations", devis.getNotesReparation() == null ? "" : devis.getNotesReparation()));
    }

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

    private String safe(String val) { return val != null ? val : ""; }
}
