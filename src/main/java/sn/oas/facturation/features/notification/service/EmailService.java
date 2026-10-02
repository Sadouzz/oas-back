package sn.oas.facturation.features.notification.service;

import java.io.File;

public interface EmailService {
    void sendSimpleEmail(String to, String subject, String text);
    void sendHtmlEmail(String to, String subject, String htmlBody);
    void sendEmailWithAttachment(String to, String subject, String text, String attachmentName, File attachment);
    void sendEmailWithAttachment(String to, String subject, String text, String attachmentName, byte[] attachmentData, String mimeType);
}
