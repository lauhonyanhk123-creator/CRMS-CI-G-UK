package com.crms.service.impl;

import com.crms.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final String from;
    private final String adminAddress;
    private final boolean smtpEnabled;

    public EmailServiceImpl(
            JavaMailSender mailSender,
            @Value("${crms.mail.from:noreply@crms.local}") String from,
            @Value("${crms.mail.admin-address:admin@crms.local}") String adminAddress,
            @Value("${spring.mail.host:disabled}") String mailHost) {
        this.mailSender = mailSender;
        this.from = from;
        this.adminAddress = adminAddress;
        this.smtpEnabled = !"disabled".equalsIgnoreCase(mailHost) && !mailHost.isBlank();
        if (!this.smtpEnabled) {
            log.warn("SMTP not configured (MAIL_HOST not set). Email notifications are disabled.");
        }
    }

    @Override
    public void sendSimple(String to, String subject, String text) {
        if (!smtpEnabled) {
            log.info("[EMAIL DISABLED] Would have sent to {}: {}", to, subject);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Email sent to {}: {}", to, subject);
        } catch (MailException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    @Override
    public void sendAdminAlert(String subject, String text) {
        sendSimple(adminAddress, subject, text);
    }
}
