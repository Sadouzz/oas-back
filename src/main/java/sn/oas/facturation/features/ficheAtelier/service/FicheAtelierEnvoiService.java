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
import sn.oas.facturation.features.notification.service.ClientEmailTemplateService;
import sn.oas.facturation.features.notification.service.EmailAttachment;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FicheAtelierEnvoiService {
    private final FicheAtelierRepository repository;
    private final FicheAtelierPdfService pdfService;
    private final EmailService emailService;
    private final ClientEmailTemplateService clientEmailTemplateService;

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
            String immatriculation = fiche.getVehicule() == null ? "—" : fiche.getVehicule().getImmatriculation();
            String html = clientEmailTemplateService.ficheAtelier(
                    fiche.getClient().getFirstName(), fiche.getNumero(), immatriculation);
            emailService.sendHtmlEmailWithAttachments(email, "Votre fiche atelier " + fiche.getNumero(), html, List.of(
                    new EmailAttachment("Fiche-Atelier-" + fiche.getNumero() + ".pdf", fichePdf, "application/pdf"),
                    new EmailAttachment("Conditions-generales-" + fiche.getNumero() + ".pdf", conditionsPdf, "application/pdf")));
        } catch (RuntimeException e) {
            log.error("Fiche atelier {} : préparation de l'email impossible", event.ficheId(), e);
        }
    }

}
