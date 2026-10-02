package sn.oas.facturation.features.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Async
    @Override
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            // message.setFrom("votreadresse@gmail.com"); // Optionnel si configuré par défaut
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
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
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
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);
            
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
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);
            
            ByteArrayResource resource = new ByteArrayResource(attachmentData);
            helper.addAttachment(attachmentName, resource, mimeType);
            
            javaMailSender.send(message);
            log.info("Email avec pièce jointe en mémoire envoyé avec succès à {}", to);
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email avec pièce jointe en mémoire à {}: {}", to, e.getMessage());
        }
    }
}
