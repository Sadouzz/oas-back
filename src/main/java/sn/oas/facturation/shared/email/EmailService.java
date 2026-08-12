package sn.oas.facturation.shared.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.vehicule.data.entity.Vehicule;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /**
     * Envoyée en tâche de fond (voir EmailConfig) : un échec d'envoi ne doit jamais faire échouer
     * la création de la fiche atelier elle-même, on se contente de logger.
     */
    @Async("emailTaskExecutor")
    public void sendFicheAtelierCreatedEmail(Client client, FicheAtelier fiche, Vehicule vehicule) {
        if (client == null || client.getEmail() == null || client.getEmail().isBlank()) {
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress, "OAS - Orient Auto Service");
            helper.setTo(client.getEmail());
            helper.setSubject("Fiche atelier " + fiche.getNumero() + " créée pour votre véhicule");
            helper.setText(buildHtml(client, fiche, vehicule), true);

            mailSender.send(message);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.warn("Échec de l'envoi de l'email de création de fiche atelier {} au client {} : {}",
                    fiche.getNumero(), client.getId(), e.getMessage());
        } catch (Exception e) {
            log.warn("Erreur inattendue lors de l'envoi de l'email de fiche atelier {} : {}",
                    fiche.getNumero(), e.getMessage());
        }
    }

    private String buildHtml(Client client, FicheAtelier fiche, Vehicule vehicule) {
        String vehiculeLabel = vehicule != null
                ? (nullToEmpty(vehicule.getMarque()) + " " + nullToEmpty(vehicule.getModele())
                    + " (" + nullToEmpty(vehicule.getImmatriculation()) + ")").trim()
                : "votre véhicule";
        String garageLabel = fiche.getGarage() != null ? fiche.getGarage().getNom() : null;
        String motif = nullToEmpty(fiche.getDescriptionTravaux());
        String suiviUrl = frontendUrl + "/espace-client/interventions";

        return "<div style=\"font-family:Arial,Helvetica,sans-serif;max-width:560px;margin:0 auto;color:#1c2b3a;\">"
                + "<div style=\"background:#0c2742;padding:24px;text-align:center;\">"
                + "<h1 style=\"color:#ffffff;margin:0;font-size:20px;\">OAS — Orient Auto Service</h1>"
                + "</div>"
                + "<div style=\"padding:24px;background:#ffffff;border:1px solid #e4e8ee;border-top:none;\">"
                + "<p>Bonjour " + nullToEmpty(client.getFirstName()) + ",</p>"
                + "<p>Une fiche atelier vient d'être ouverte pour <strong>" + vehiculeLabel + "</strong>.</p>"
                + "<table style=\"width:100%;border-collapse:collapse;margin:16px 0;font-size:14px;\">"
                + "<tr><td style=\"padding:6px 0;color:#6b7a8d;\">Numéro de fiche</td><td style=\"padding:6px 0;font-weight:bold;\">" + fiche.getNumero() + "</td></tr>"
                + (motif.isEmpty() ? "" : "<tr><td style=\"padding:6px 0;color:#6b7a8d;\">Motif</td><td style=\"padding:6px 0;\">" + motif + "</td></tr>")
                + (garageLabel != null ? "<tr><td style=\"padding:6px 0;color:#6b7a8d;\">Garage</td><td style=\"padding:6px 0;\">" + garageLabel + "</td></tr>" : "")
                + "</table>"
                + "<p>Vous recevrez également une notification dans votre espace client à chaque changement d'étape de la réparation.</p>"
                + "<div style=\"text-align:center;margin:28px 0;\">"
                + "<a href=\"" + suiviUrl + "\" style=\"background:#ef6c1a;color:#ffffff;text-decoration:none;padding:12px 28px;border-radius:8px;font-weight:bold;display:inline-block;\">Suivre ma réparation</a>"
                + "</div>"
                + "<p style=\"font-size:12px;color:#9aa7b5;\">Cet email a été envoyé automatiquement, merci de ne pas y répondre directement.</p>"
                + "</div>"
                + "</div>";
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
