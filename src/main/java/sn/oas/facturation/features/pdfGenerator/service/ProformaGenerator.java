package sn.oas.facturation.features.pdfGenerator.service;

import org.springframework.stereotype.Service;
import sn.oas.facturation.features.proforma.data.entity.Proforma;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class ProformaGenerator {

    private final HtmlToPdfService htmlToPdfService;

    public ProformaGenerator(HtmlToPdfService htmlToPdfService) {
        this.htmlToPdfService = htmlToPdfService;
    }

    public byte[] genererProformaPdf(Proforma p) {
        String html = construireHtmlProforma(p);
        return htmlToPdfService.genererHtmlEnPdf(html);
    }

    private String construireHtmlProforma(Proforma p) {
        String numero   = safe(p.getNumero());
        String agentNom = p.getAgent() != null
                ? p.getAgent().getFirstName() + " " + p.getAgent().getLastName() : "";
        String dateDoc  = p.getDateCreation() != null
                ? p.getDateCreation().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) : "";
        String numBc    = p.getBonDeCommande() != null ? safe(p.getBonDeCommande().getNumero()) : "";

        String clientNum = "", clientNom = "", clientTel = "", clientEmail = "", clientAdresse = "";
        String annee = "", marque = "", modele = "", immat = "", chassis = "";
        String km = p.getKilometrage() != null ? String.valueOf(p.getKilometrage().longValue()) : "";

        if (p.getOrdreReparation() != null && p.getOrdreReparation().getVehicule() != null) {
            var v = p.getOrdreReparation().getVehicule();
            annee  = v.getAnnee() != null ? String.valueOf(v.getAnnee()) : "";
            marque = safe(v.getMarque());
            modele = safe(v.getModele());
            immat  = safe(v.getImmatriculation());
            chassis = safe(v.getNumeroChassis());
            if (v.getClient() != null) {
                clientNum    = String.valueOf(v.getClient().getId());
                clientNom    = v.getClient().getFirstName() + " " + v.getClient().getLastName();
                clientTel    = safe(v.getClient().getPhone());
                clientEmail  = safe(v.getClient().getEmail());
                clientAdresse = safe(v.getClient().getAdresse());
            }
        }

        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.FRENCH);
        sym.setGroupingSeparator('.'); sym.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("#,##0", sym);

        String totalHT     = p.getMontantHT()     != null ? df.format(p.getMontantHT())     : "0";
        String totalTVA    = p.getMontantTVA()    != null ? df.format(p.getMontantTVA())    : "0";
        String totalTimbre = p.getMontantTimbre() != null ? df.format(p.getMontantTimbre()) : "0";
        String totalTTC    = p.getMontantTTC()    != null ? df.format(p.getMontantTTC())    : "0";
        String totalTTCLettres = montantEnLettres(p.getMontantTTC());

        String remarques = "";
        if (p.getRemarque() != null) {
            remarques = p.getRemarque()
                .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\r\n", "<br/>").replace("\n", "<br/>");
        }

        // Lignes pièces
        StringBuilder lignesPieces = new StringBuilder();
        if (p.getLignesFacturationPieces() != null) {
            for (var l : p.getLignesFacturationPieces()) {
                String ref = l.getPiece() != null ? safe(l.getPiece().getReference()) : "";
                String des = l.getPiece() != null ? safe(l.getPiece().getDesignation()) : safe(l.getDesignationPds());
                int qte = l.getQuantite() != null ? l.getQuantite() : 0;
                int pu  = l.getPrix()     != null ? l.getPrix()     : 0;
                lignesPieces.append("<tr>")
                    .append("<td>").append(ref).append("</td>")
                    .append("<td>").append(des).append("</td>")
                    .append("<td>").append(qte).append("</td>")
                    .append("<td>0</td>")
                    .append("<td>").append(df.format(pu)).append("</td>")
                    .append("<td>").append(df.format((long)qte * pu)).append("</td>")
                    .append("</tr>");
            }
        }

        // Lignes main d'œuvre
        StringBuilder lignesMo = new StringBuilder();
        if (p.getLignesFacturationMainDoeuvres() != null) {
            for (var l : p.getLignesFacturationMainDoeuvres()) {
                String ref = l.getMainDoeuvre() != null ? String.valueOf(l.getMainDoeuvre().getId()) : "";
                String des = l.getMainDoeuvre() != null ? safe(l.getMainDoeuvre().getDescription()) : "";
                int h = l.getNbreHeure()    != null ? l.getNbreHeure()    : 0;
                int t = l.getTarifHoraire() != null ? l.getTarifHoraire() : 0;
                lignesMo.append("<tr>")
                    .append("<td>").append(ref).append("</td>")
                    .append("<td>").append(des).append("</td>")
                    .append("<td>").append(h).append("</td>")
                    .append("<td>").append(df.format(t)).append("</td>")
                    .append("<td>").append(df.format((long)h * t)).append("</td>")
                    .append("</tr>");
            }
        }

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
            + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
            + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"fr\">\n"
            + "<head>\n"
            + "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n"
            + "  <title>Proforma " + numero + "</title>\n"
            + "  <style type=\"text/css\">\n"
            + "    body { font-family: Helvetica, Arial, sans-serif; font-size: 13px; margin: 20px; }\n"
            + "    table { background: white; width: 100%; border-collapse: collapse; }\n"
            + "    .ht th { vertical-align: top; padding: 4px 8px; font-weight: normal; }\n"
            + "    .ht th.l { width: 38%; text-align: left; }\n"
            + "    .ht th.m { width: 20%; text-align: left; }\n"
            + "    .ht th.r { width: 42%; text-align: left; }\n"
            + "    .title { font-size: 14px; font-weight: bold; text-decoration: underline; }\n"
            + "    .dt { font-size: 12px; border: 1px solid #000; }\n"
            + "    .dt th, .dt td { border: 1px solid #000; padding: 4px 6px; text-align: center; }\n"
            + "    .dt thead tr { background-color: #f5f5f0; }\n"
            + "    .sc { text-align: center; font-size: 12px; margin: 6px 0; }\n"
            + "    .tt { font-size: 14px; border: 1px solid #000; margin: 0 auto; width: auto; }\n"
            + "    .tt th { border: 1px solid #000; padding: 4px 10px; background-color: #f5f5f0; }\n"
            + "    .tt td { border: 1px solid #000; padding: 4px 10px; text-align: center; font-weight: bold; }\n"
            + "    u { text-decoration: underline; }\n"
            + "  </style>\n"
            + "</head>\n<body>\n"

            // En-tête
            + "<table class=\"ht\"><tr>\n"
            + "  <th class=\"l\"><span class=\"title\">PROFORMA :</span><br/>Num. DK/" + numero + "<br/><br/>\n"
            + "    <strong><u>BON DE COMMANDE :</u></strong><br/> " + numBc + "<br/><br/></th>\n"
            + "  <th class=\"m\">Agent : " + agentNom + "<br/><br/><br/><br/><br/></th>\n"
            + "  <th class=\"r\">Dakar le " + dateDoc + "<br/><br/>\n"
            + "    <u>CLIENT</u> Num. " + clientNum + "<br/>\n"
            + "    Nom : " + clientNom + "<br/>\n"
            + "    T&#233;l : " + clientTel + "<br/>\n"
            + "    Email : " + clientEmail + "<br/>\n"
            + "    Adresse : " + clientAdresse + "<br/></th>\n"
            + "</tr></table>\n<br/>\n"

            // Véhicule
            + "<table class=\"dt\"><thead><tr>\n"
            + "  <th style=\"width:14%\">Ann&#233;e</th><th style=\"width:16%\">Marque</th>\n"
            + "  <th style=\"width:16%\">Mod&#232;le</th><th style=\"width:17%\">No. immatriculation</th>\n"
            + "  <th style=\"width:11%\">Kilom&#233;trage</th><th style=\"width:26%\">No. chassie</th>\n"
            + "</tr></thead><tbody><tr>\n"
            + "  <td>" + annee + "</td><td>" + marque + "</td><td>" + modele + "</td>\n"
            + "  <td>" + immat + "</td><td>" + km + "</td><td>" + chassis + "</td>\n"
            + "</tr></tbody></table>\n<br/>\n"

            // Pièces
            + "<table class=\"dt\"><thead><tr>\n"
            + "  <th style=\"width:15%\">R&#233;f&#233;rence</th>\n"
            + "  <th style=\"width:43%\">D&#233;signation</th>\n"
            + "  <th style=\"width:7%\">Qt&#233;</th>\n"
            + "  <th style=\"width:7%\">Rem.(%)</th>\n"
            + "  <th style=\"width:13%\">Prix unitaire</th>\n"
            + "  <th style=\"width:13%\">Total</th>\n"
            + "</tr></thead><tbody>" + lignesPieces + "</tbody></table>\n<br/>\n"

            // Main d'œuvre
            + "<div class=\"sc\">Mains d&#39;oeuvre</div>\n"
            + "<table class=\"dt\"><thead><tr>\n"
            + "  <th style=\"width:15%\">R&#233;f&#233;rence</th>\n"
            + "  <th style=\"width:50%\">D&#233;signation</th>\n"
            + "  <th style=\"width:7%\">Heures</th>\n"
            + "  <th style=\"width:14%\">Prix unitaire</th>\n"
            + "  <th style=\"width:14%\">Total</th>\n"
            + "</tr></thead><tbody>" + lignesMo + "</tbody></table>\n<br/>\n"

            // Remarques
            + "<table class=\"dt\"><thead><tr><th>Remarques</th></tr></thead>\n"
            + "<tbody><tr><td style=\"text-align:left;\">" + remarques + "</td></tr></tbody></table>\n"
            + "<br/><br/>\n"

            // Totaux
            + "<table class=\"tt\"><thead><tr>\n"
            + "  <th style=\"width:22%\">Total HT</th>\n"
            + "  <th style=\"width:20%\">TVA (18%)</th>\n"
            + "  <th style=\"width:20%\">Timbre</th>\n"
            + "  <th style=\"width:22%\">Montant Total TTC</th>\n"
            + "</tr></thead><tbody>\n"
            + "  <tr><td>" + totalHT + "</td><td>" + totalTVA + "</td><td>" + totalTimbre + "</td><td>" + totalTTC + "</td></tr>\n"
            + "  <tr><td colspan=\"4\" style=\"text-align:center;font-style:italic;font-weight:normal;\">"
            + totalTTCLettres + "</td></tr>\n"
            + "</tbody></table>\n"
            + "</body>\n</html>\n";
    }

    private String safe(String val) { return val != null ? val : ""; }

    private String montantEnLettres(BigDecimal montant) {
        if (montant == null) return "zéro franc CFA";
        long val = montant.setScale(0, RoundingMode.HALF_UP).longValue();
        if (val == 0) return "zéro franc CFA";
        return convertirEntierEnLettres(val) + (val > 1 ? " francs CFA" : " franc CFA");
    }

    private String convertirEntierEnLettres(long n) {
        if (n < 0) return "moins " + convertirEntierEnLettres(-n);
        if (n == 0) return "";
        String[] u = {"","un","deux","trois","quatre","cinq","six","sept","huit","neuf",
                      "dix","onze","douze","treize","quatorze","quinze","seize",
                      "dix-sept","dix-huit","dix-neuf"};
        String[] d = {"","","vingt","trente","quarante","cinquante","soixante",
                      "soixante","quatre-vingt","quatre-vingt"};
        if (n < 20) return u[(int)n];
        if (n < 100) {
            int diz = (int)(n/10), unite = (int)(n%10);
            if (diz == 7 || diz == 9) return d[diz] + "-" + u[(int)(n - diz*10 + 10)];
            return d[diz] + (unite > 0 ? (unite == 1 && diz != 8 ? " et " : "-") + u[unite] : (diz == 8 ? "s" : ""));
        }
        if (n < 1000) {
            long c = n/100, r = n%100;
            return (c == 1 ? "cent" : u[(int)c] + " cent" + (r == 0 && c > 1 ? "s" : ""))
                   + (r > 0 ? " " + convertirEntierEnLettres(r) : "");
        }
        if (n < 1_000_000L) {
            long m = n/1000, r = n%1000;
            return (m == 1 ? "mille" : convertirEntierEnLettres(m) + " mille")
                   + (r > 0 ? " " + convertirEntierEnLettres(r) : "");
        }
        if (n < 1_000_000_000L) {
            long m = n/1_000_000, r = n%1_000_000;
            return convertirEntierEnLettres(m) + (m > 1 ? " millions" : " million")
                   + (r > 0 ? " " + convertirEntierEnLettres(r) : "");
        }
        long m = n/1_000_000_000L, r = n%1_000_000_000L;
        return convertirEntierEnLettres(m) + (m > 1 ? " milliards" : " milliard")
               + (r > 0 ? " " + convertirEntierEnLettres(r) : "");
    }
}