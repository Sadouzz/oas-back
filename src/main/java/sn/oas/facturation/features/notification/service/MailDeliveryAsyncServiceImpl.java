package sn.oas.facturation.features.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailDeliveryAsyncServiceImpl implements MailDeliveryAsyncService {

    private final EmailService emailService;

    @Async("mailTaskExecutor")
    @Override
    public void executeAsync(Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            log.error("Erreur lors de l'exécution d'une tâche d'envoi d'email asynchrone: {}", e.getMessage(), e);
        }
    }

    @Async("mailTaskExecutor")
    @Override
    public void sendEmailWithPdfAttachmentAsync(String to, String subject, String body, String attachmentName, Supplier<byte[]> pdfSupplier, boolean html) {
        if (to == null || to.trim().isEmpty()) {
            return;
        }
        try {
            byte[] pdfBytes = pdfSupplier.get();
            if (pdfBytes == null || pdfBytes.length == 0) {
                log.warn("Aucune donnée PDF générée pour l'email à {}", to);
                return;
            }

            if (html) {
                emailService.sendHtmlEmailWithAttachment(to, subject, body, attachmentName, pdfBytes, "application/pdf");
            } else {
                emailService.sendEmailWithAttachment(to, subject, body, attachmentName, pdfBytes, "application/pdf");
            }
        } catch (Exception e) {
            log.error("Échec de la génération/envoi du document par email à {} (sujet: {}): {}", to, subject, e.getMessage(), e);
        }
    }
}

