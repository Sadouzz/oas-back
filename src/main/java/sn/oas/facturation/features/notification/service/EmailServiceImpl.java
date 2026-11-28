package sn.oas.facturation.features.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final ClientEmailTemplateService clientEmailTemplateService;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Async
    @Override
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = createHelper(message);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, clientEmailTemplateService.notification(subject, text));
            addOasLogo(helper);
            javaMailSender.send(message);
            log.info("Email envoyé avec succès à {}", to);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à {}: {}", to, e.getMessage());
        }
    }

    @Async
    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = createHelper(message);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            addOasLogo(helper);
            javaMailSender.send(message);
            log.info("Email HTML envoyé avec succès à {}", to);
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email HTML à {}: {}", to, e.getMessage());
        }
    }

    @Async
    @Override
    public void sendEmailWithAttachment(String to, String subject, String text, String attachmentName, File attachment) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = createHelper(message);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, clientEmailTemplateService.notification(subject, text));
            addOasLogo(helper);
            
            FileSystemResource file = new FileSystemResource(attachment);
            helper.addAttachment(attachmentName, file);
            
            javaMailSender.send(message);
            log.info("Email avec pièce jointe envoyé avec succès à {}", to);
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email avec pièce jointe à {}: {}", to, e.getMessage());
        }
    }

    @Async
    @Override
    public void sendEmailWithAttachment(String to, String subject, String text, String attachmentName, byte[] attachmentData, String mimeType) {
        sendAttachmentEmail(to, subject, text, attachmentName, attachmentData, mimeType, false);
    }

    @Async
    @Override
    public void sendHtmlEmailWithAttachment(String to, String subject, String htmlBody, String attachmentName, byte[] attachmentData, String mimeType) {
        sendAttachmentEmail(to, subject, htmlBody, attachmentName, attachmentData, mimeType, true);
    }

    @Override
    public void sendHtmlEmailWithAttachmentSync(String to, String subject, String htmlBody, String attachmentName,
                                                byte[] attachmentData, String mimeType) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = createHelper(message);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        addOasLogo(helper);
        helper.addAttachment(attachmentName, new ByteArrayResource(attachmentData), mimeType);
        javaMailSender.send(message);
        log.info("Email HTML avec pièce jointe envoyé et accepté par SMTP pour {}", to);
    }

    @Async
    @Override
    public void sendHtmlEmailWithAttachments(String to, String subject, String htmlBody, List<EmailAttachment> attachments) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = createHelper(message);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            addOasLogo(helper);
            for (EmailAttachment attachment : attachments) {
                helper.addAttachment(attachment.name(), new ByteArrayResource(attachment.data()), attachment.mimeType());
            }
            javaMailSender.send(message);
            log.info("Email HTML avec {} pièces jointes envoyé à {}", attachments.size(), to);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email HTML avec pièces jointes à {}: {}", to, e.getMessage());
        }
    }

    private void sendAttachmentEmail(String to, String subject, String body, String attachmentName, byte[] attachmentData, String mimeType, boolean html) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = createHelper(message);
            helper.setTo(to);
            helper.setSubject(subject);
            if (html) {
                helper.setText(body, true);
            } else {
                helper.setText(body, clientEmailTemplateService.notification(subject, body));
            }
            addOasLogo(helper);
            
            ByteArrayResource resource = new ByteArrayResource(attachmentData);
            helper.addAttachment(attachmentName, resource, mimeType);
            
            javaMailSender.send(message);
            log.info("Email avec pièce jointe en mémoire envoyé avec succès à {}", to);
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email avec pièce jointe en mémoire à {}: {}", to, e.getMessage());
        }
    }

    private void addOasLogo(MimeMessageHelper helper) throws MessagingException {
        helper.addInline("oasLogo", new ClassPathResource("branding/oas-logo.png"), "image/png");
    }

    private MimeMessageHelper createHelper(MimeMessage message) throws MessagingException {
        MimeMessageHelper helper = new MimeMessageHelper(
                message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
        if (fromAddress != null && !fromAddress.isBlank()) {
            helper.setFrom(fromAddress.trim());
        }
        return helper;
    }
}
