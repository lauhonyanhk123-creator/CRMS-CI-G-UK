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

    public EmailServiceImpl(
            JavaMailSender mailSender,
            @Value("${crms.mail.from}") String from,
            @Value("${crms.mail.admin-address}") String adminAddress) {
        this.mailSender = mailSender;
        this.from = from;
        this.adminAddress = adminAddress;
    }

    @Override
    public void sendSimple(String to, String subject, String text) {
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
