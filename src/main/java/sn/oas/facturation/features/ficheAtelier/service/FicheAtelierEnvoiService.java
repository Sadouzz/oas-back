package sn.oas.facturation.features.ficheAtelier.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.features.ficheAtelier.repository.FicheAtelierRepository;
import sn.oas.facturation.features.notification.service.EmailService;
import sn.oas.facturation.features.notification.service.EmailAttachment;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FicheAtelierEnvoiService {
    private final FicheAtelierRepository repository;
    private final FicheAtelierPdfService pdfService;
    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void envoyerApresCreation(FicheAtelierCreee event) {
        FicheAtelier fiche = repository.findById(event.ficheId()).orElse(null);
        if (fiche == null || fiche.getClient() == null) return;
        String email = fiche.getClient().getEmail();
        if (email == null || email.isBlank()) {
            log.warn("Fiche atelier {} : aucun email client, envoi à effectuer manuellement", event.ficheId());
            return;
        }
        try {
            byte[] fichePdf = pdfService.genererFicheEmail(event.ficheId());
            byte[] conditionsPdf = pdfService.genererConditionsEmail(event.ficheId());
            String prenom = escape(fiche.getClient().getFirstName());
            String html = """
                    <div style="margin:0;background:#f3f5f7;padding:32px 12px;font-family:Arial,Helvetica,sans-serif;color:#243447">
                      <div style="max-width:620px;margin:0 auto;background:#ffffff;border:1px solid #e2e7ec;border-radius:10px;overflow:hidden">
                        <div style="background:#12304a;padding:24px 30px;color:#ffffff">
                          <div style="font-size:12px;letter-spacing:1.5px;text-transform:uppercase;color:#d9e4ef">Orient Auto Service</div>
                          <h1 style="font-size:21px;margin:10px 0 0">Votre fiche atelier est confirmée</h1>
                        </div>
                        <div style="padding:28px 30px;line-height:1.6;font-size:15px">
                          <p>Bonjour %s,</p>
                          <p>Votre fiche atelier <strong>%s</strong> a bien été enregistrée pour le véhicule <strong>%s</strong>.</p>
                          <p>Vous trouverez en pièces jointes la fiche atelier signée ainsi que les conditions générales de réparation acceptées lors de la réception.</p>
                          <p>Nous vous invitons à conserver ces documents. Notre équipe reste à votre disposition pour toute question relative à la prise en charge de votre véhicule.</p>
                          <p style="margin-top:26px">Cordialement,<br><strong>L’équipe Orient Auto Service</strong></p>
                        </div>
                        <div style="border-top:1px solid #e8edf1;padding:14px 30px;color:#718096;font-size:12px">Message automatique — merci de ne pas répondre à cette adresse.</div>
                      </div>
                    </div>
                    """.formatted(prenom, escape(fiche.getNumero()), escape(fiche.getVehicule() == null ? "" : fiche.getVehicule().getImmatriculation()));
            emailService.sendHtmlEmailWithAttachments(email, "Votre fiche atelier " + fiche.getNumero(), html, List.of(
                    new EmailAttachment("Fiche-Atelier-" + fiche.getNumero() + ".pdf", fichePdf, "application/pdf"),
                    new EmailAttachment("Conditions-generales-" + fiche.getNumero() + ".pdf", conditionsPdf, "application/pdf")));
        } catch (RuntimeException e) {
            log.error("Fiche atelier {} : préparation de l'email impossible", event.ficheId(), e);
        }
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
