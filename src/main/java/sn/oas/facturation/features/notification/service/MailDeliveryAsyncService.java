package sn.oas.facturation.features.notification.service;

import java.util.function.Supplier;

public interface MailDeliveryAsyncService {
    void executeAsync(Runnable task);
    void sendEmailWithPdfAttachmentAsync(String to, String subject, String body, String attachmentName, Supplier<byte[]> pdfSupplier, boolean html);
}

