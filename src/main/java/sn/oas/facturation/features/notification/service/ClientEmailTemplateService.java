package sn.oas.facturation.features.notification.service;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Shared, email-client-safe presentation and French copy for messages sent to clients.
 * Dynamic values are always escaped before they are inserted into the HTML document.
 */
@Service
public class ClientEmailTemplateService {

    public String ordreCree(String prenom, String numero, String immatriculation) {
        return render(
                "SUIVI DE VOTRE VÉHICULE",
                "Votre prise en charge est ouverte",
                "Le dossier de prise en charge de votre véhicule est maintenant ouvert chez Orient Auto Service. Il permet à notre équipe de suivre chaque étape de l’intervention et de centraliser les informations utiles.",
                prenom,
                List.of(new Detail("Référence du dossier", numero), new Detail("Véhicule", immatriculation)),
                "Les prochaines étapes",
                "Notre équipe examine les éléments relevés à la réception. Les travaux envisagés et leur coût vous seront présentés pour validation avant leur réalisation.",
                "Conservez la référence de votre dossier pour faciliter vos échanges avec l’atelier."
        );
    }

    public String vehiculePret(String prenom, String numero, String immatriculation) {
        return render(
                "SUIVI DE VOTRE VÉHICULE",
                "Votre véhicule est prêt",
                "Les interventions prévues sont terminées. Votre véhicule peut désormais être récupéré auprès de votre atelier.",
                prenom,
                List.of(new Detail("Référence du dossier", numero), new Detail("Véhicule", immatriculation)),
                "Avant votre déplacement",
                "Nous vous invitons à contacter votre atelier pour confirmer ses horaires et organiser le retrait de votre véhicule.",
                "Notre équipe reste disponible pour répondre à vos questions sur la restitution."
        );
    }

    public String facture(String prenom, String numero) {
        return render(
                "DOCUMENT DE FACTURATION",
                "Votre facture est disponible",
                "La facture correspondant à votre dossier est jointe à ce message. Elle récapitule les prestations et les montants associés.",
                prenom,
                List.of(new Detail("Référence de la facture", numero)),
                "Pour le règlement",
                "Retrouvez les modalités de paiement et le suivi de votre dossier dans votre espace client. Si vous avez une question sur une ligne de la facture, votre atelier pourra vous apporter les précisions nécessaires.",
                "La facture est jointe au format PDF. Nous vous conseillons de la conserver avec vos documents d’entretien."
        );
    }

    public String proforma(String prenom, String numero, String informationComplementaire) {
        String footer = "Le proforma est joint au format PDF et reste consultable dans votre espace client.";
        if (hasText(informationComplementaire)) footer += " " + informationComplementaire;
        return render(
                "DOCUMENT À CONSULTER",
                "Votre proposition de travaux est prête",
                "Nous vous transmettons le proforma associé à votre véhicule. Ce document détaille les travaux envisagés et leur estimation afin que vous puissiez les examiner avant de prendre votre décision.",
                prenom,
                List.of(new Detail("Référence du proforma", numero)),
                "Votre validation",
                "Consultez le document joint puis donnez votre accord depuis votre espace client. Les travaux prévus sur ce document restent soumis à votre validation.",
                footer
        );
    }

    public String factureDepuisProforma(String prenom, String numeroProforma, String numeroFacture) {
        return render(
                "DOCUMENT DE FACTURATION",
                "Votre facture remplace le proforma",
                "Le proforma a été converti en facture. Le document définitif est joint à ce message pour vos démarches et votre suivi.",
                prenom,
                List.of(new Detail("Proforma", numeroProforma), new Detail("Facture", numeroFacture)),
                "À retenir",
                "Utilisez désormais la référence de facture pour toute question relative au règlement ou au suivi de ce dossier.",
                "La facture est jointe au format PDF et disponible dans votre espace client."
        );
    }

    public String devis(String prenom, String numero, String immatriculation) {
        return render(
                "DOCUMENT À CONSULTER",
                "Votre devis prévisionnel est prêt",
                "Vous trouverez en pièce jointe le devis prévisionnel préparé pour votre véhicule. Il vous permet de prendre connaissance de l’estimation avant la suite de la prise en charge.",
                prenom,
                List.of(new Detail("Référence du devis", numero), new Detail("Véhicule", immatriculation)),
                "Besoin d’une précision ?",
                "Votre atelier peut vous expliquer le contenu du devis et répondre à vos questions avant toute décision.",
                "Le devis est joint au format PDF. Pensez à le conserver pour vos échanges avec l’atelier."
        );
    }

    public String diagnostic(String prenom, String immatriculation) {
        return render(
                "DOCUMENT DE SUIVI",
                "Le diagnostic de votre véhicule est disponible",
                "Le diagnostic réalisé dans le cadre de votre prise en charge est joint à ce message. Il rassemble les constats consignés par l’équipe technique.",
                prenom,
                List.of(new Detail("Véhicule", immatriculation)),
                "Pour la suite",
                "Prenez le temps de consulter le document. Votre atelier pourra vous présenter les constats et vous expliquer les interventions éventuellement recommandées.",
                "Le diagnostic est joint au format PDF et peut être conservé avec le carnet d’entretien de votre véhicule."
        );
    }

    public String ficheAtelier(String prenom, String numero, String immatriculation) {
        return render(
                "DOCUMENTS DE RÉCEPTION",
                "Votre réception atelier est confirmée",
                "La réception de votre véhicule a été enregistrée. Vous trouverez en pièces jointes la fiche atelier ainsi que les conditions générales de réparation acceptées lors de la réception.",
                prenom,
                List.of(new Detail("Référence de la fiche", numero), new Detail("Véhicule", immatriculation)),
                "À conserver",
                "Ces documents récapitulent les informations de réception et les conditions associées à la prise en charge. Gardez-les à portée de main pendant le suivi de votre véhicule.",
                "Deux documents PDF sont joints : la fiche atelier et les conditions générales de réparation."
        );
    }

    public String notification(String titre, String message) {
        return render(
                "INFORMATION CLIENT",
                titre,
                message,
                null,
                List.of(),
                "Votre espace client",
                "Retrouvez le suivi de votre véhicule et les informations liées à votre dossier dans votre espace client.",
                "Vous recevez ce message au titre du suivi de votre relation avec Orient Auto Service."
        );
    }

    public String bienvenue(String prenom) {
        return render(
                "BIENVENUE CHEZ OAS",
                "Votre espace client est prêt",
                "Votre compte Orient Auto Service a bien été créé. Vous pouvez désormais utiliser votre espace client pour retrouver vos véhicules, vos rendez-vous et les informations de suivi mises à votre disposition.",
                prenom,
                List.of(),
                "Un point de suivi centralisé",
                "Vos documents et les principales étapes de prise en charge sont regroupés au même endroit pour vous permettre de suivre plus facilement l’entretien de votre véhicule.",
                "Nous vous remercions de votre confiance et vous souhaitons la bienvenue chez Orient Auto Service."
        );
    }

    public String nouvelleConnexion(String prenom, String adresseIp) {
        return render(
                "SÉCURITÉ DE VOTRE COMPTE",
                "Une nouvelle connexion a été détectée",
                "Une connexion à votre compte Orient Auto Service vient d’être enregistrée. Si vous en êtes à l’origine, aucune action n’est nécessaire.",
                prenom,
                List.of(new Detail("Adresse IP", adresseIp)),
                "Vous ne reconnaissez pas cette connexion ?",
                "Par précaution, modifiez votre mot de passe et contactez le support de votre atelier afin de sécuriser votre compte.",
                "Ce message est envoyé automatiquement pour vous aider à garder le contrôle de votre compte."
        );
    }

    private String render(String eyebrow, String title, String intro, String prenom,
                          List<Detail> details, String sectionTitle, String sectionBody, String footerNote) {
        StringBuilder rows = new StringBuilder();
        for (Detail detail : details) {
            rows.append("<tr><td style=\"padding:13px 16px;border-bottom:1px solid #e7ecf3;color:#6c7c93;font-size:12px;letter-spacing:.04em;text-transform:uppercase;width:42%;\">")
                    .append(escape(detail.label()))
                    .append("</td><td style=\"padding:13px 16px;border-bottom:1px solid #e7ecf3;color:#13355c;font-size:15px;font-weight:700;\">")
                    .append(escape(detail.value()))
                    .append("</td></tr>");
        }
        String detailsBlock = rows.isEmpty() ? "" : """
                <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="border:1px solid #e7ecf3;border-radius:10px;border-collapse:separate;border-spacing:0;overflow:hidden;margin:24px 0;background:#fbfcfd">
                  {{DETAIL_ROWS}}
                </table>
                """.replace("{{DETAIL_ROWS}}", rows.toString());
        String greeting = hasText(prenom)
                ? "Bonjour " + escape(prenom.trim()) + ","
                : "Bonjour,";

        String html = """
                <!doctype html>
                <html lang="fr">
                <head>
                  <meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
                  <meta name="color-scheme" content="light"><meta name="supported-color-schemes" content="light">
                  <title>{{TITLE}}</title>
                  <style>@media screen and (max-width:620px){.email-shell{padding:16px 8px!important}.email-card{border-radius:12px!important}.email-content{padding:26px 22px!important}.email-heading{font-size:25px!important;line-height:1.2!important}.email-brand{padding:20px 22px!important}.email-footer{padding:18px 22px!important}}</style>
                </head>
                <body style="margin:0;padding:0;background:#f4f6fa;font-family:Arial,Helvetica,sans-serif;color:#14233b;-webkit-text-size-adjust:100%">
                  <div style="display:none;max-height:0;overflow:hidden;opacity:0;color:transparent">{{PREHEADER}}</div>
                  <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background:#f4f6fa;border-collapse:collapse">
                    <tr><td class="email-shell" align="center" style="padding:38px 16px">
                      <table role="presentation" class="email-card" width="100%" cellpadding="0" cellspacing="0" style="max-width:640px;background:#ffffff;border:1px solid #e7ecf3;border-radius:16px;overflow:hidden;border-collapse:separate;border-spacing:0">
                        <tr><td style="height:5px;background:#ef1a1a;font-size:0;line-height:0">&nbsp;</td></tr>
                        <tr><td class="email-brand" style="padding:24px 34px;background:#0c2742;color:#ffffff">
                          <table role="presentation" width="100%" cellpadding="0" cellspacing="0"><tr>
                            <td width="58" valign="middle"><div style="width:50px;height:50px;padding:3px;box-sizing:border-box;border:1px solid #ef1a1a;border-radius:50%;background:#ffffff"><img src="cid:oasLogo" width="44" height="44" alt="Logo Orient Auto Service" style="display:block;width:44px;height:44px;border:0;border-radius:50%"></div></td>
                            <td valign="middle" style="padding-left:12px"><div style="color:#ffffff;font-size:14px;font-weight:700;letter-spacing:.12em">ORIENT AUTO SERVICE</div><div style="margin-top:4px;color:#b8c7d5;font-size:11px;letter-spacing:.04em">L’ENTRETIEN DE VOTRE VÉHICULE, EN TOUTE CONFIANCE</div></td>
                          </tr></table>
                        </td></tr>
                        <tr><td class="email-content" style="padding:38px 42px 34px">
                          <div style="margin-bottom:12px;color:#d61212;font-size:11px;font-weight:700;letter-spacing:.14em;text-transform:uppercase">{{EYEBROW}}</div>
                          <h1 class="email-heading" style="margin:0 0 22px;color:#0c2742;font-size:30px;line-height:1.18;font-weight:750;letter-spacing:-.02em">{{TITLE}}</h1>
                          <p style="margin:0 0 18px;color:#14233b;font-size:15px;line-height:1.75">{{INTRO}}</p>
                          <p style="margin:0;color:#14233b;font-size:15px;line-height:1.65">{{GREETING}}</p>
                          {{DETAILS}}
                          <div style="margin:26px 0 0;padding:20px 22px;border-left:3px solid #ef1a1a;border-radius:0 10px 10px 0;background:#fde2e2">
                            <div style="margin:0 0 8px;color:#13355c;font-size:14px;font-weight:700">{{SECTION_TITLE}}</div>
                            <div style="color:#3c4d66;font-size:14px;line-height:1.7">{{SECTION_BODY}}</div>
                          </div>
                          <p style="margin:26px 0 0;color:#14233b;font-size:14px;line-height:1.7">Cordialement,<br><strong style="color:#0c2742">L’équipe Orient Auto Service</strong></p>
                        </td></tr>
                        <tr><td class="email-footer" style="padding:20px 42px;background:#f8fafb;border-top:1px solid #e7ecf3">
                          <p style="margin:0;color:#6c7c93;font-size:12px;line-height:1.6">{{FOOTER_NOTE}}</p>
                          <p style="margin:10px 0 0;color:#9aa8bd;font-size:11px;line-height:1.5">Ce message est lié au suivi de votre dossier chez Orient Auto Service. Merci de ne pas partager les documents joints sans précaution.</p>
                        </td></tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """;
        return html.replace("{{TITLE}}", escape(title))
                .replace("{{PREHEADER}}", escape(intro))
                .replace("{{EYEBROW}}", escape(eyebrow))
                .replace("{{INTRO}}", escape(intro))
                .replace("{{GREETING}}", greeting)
                .replace("{{DETAILS}}", detailsBlock)
                .replace("{{SECTION_TITLE}}", escape(sectionTitle))
                .replace("{{SECTION_BODY}}", escape(sectionBody))
                .replace("{{FOOTER_NOTE}}", escape(footerNote));
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String escape(String value) {
        if (value == null) return "—";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;").replace("\n", "<br>");
    }

    private record Detail(String label, String value) { }
}
