package sn.oas.facturation.features.notification.service;

import java.io.File;
import java.util.List;
import jakarta.mail.MessagingException;

public interface EmailService {
    void sendSimpleEmail(String to, String subject, String text);
    void sendHtmlEmail(String to, String subject, String htmlBody);
    void sendEmailWithAttachment(String to, String subject, String text, String attachmentName, File attachment);
    void sendEmailWithAttachment(String to, String subject, String text, String attachmentName, byte[] attachmentData, String mimeType);
    void sendHtmlEmailWithAttachment(String to, String subject, String htmlBody, String attachmentName, byte[] attachmentData, String mimeType);
    void sendHtmlEmailWithAttachmentSync(String to, String subject, String htmlBody, String attachmentName, byte[] attachmentData, String mimeType) throws MessagingException;
    void sendHtmlEmailWithAttachments(String to, String subject, String htmlBody, List<EmailAttachment> attachments);
}
