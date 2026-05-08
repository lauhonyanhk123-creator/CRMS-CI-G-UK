package com.crms.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailServiceImpl emailService;

    private static final String FROM    = "noreply@crms.local";
    private static final String ADMIN   = "admin@crms.local";

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender, FROM, ADMIN, "localhost");
    }

    @Test
    @DisplayName("sendSimple builds and sends a SimpleMailMessage")
    void sendSimple_sendsCorrectMessage() {
        emailService.sendSimple("user@example.com", "Hello", "Body text");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();
        assertEquals(FROM, msg.getFrom());
        assertArrayEquals(new String[]{"user@example.com"}, msg.getTo());
        assertEquals("Hello", msg.getSubject());
        assertEquals("Body text", msg.getText());
    }

    @Test
    @DisplayName("sendSimple swallows MailException so callers are not disrupted")
    void sendSimple_mailException_doesNotPropagate() {
        doThrow(new MailSendException("SMTP down")).when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> emailService.sendSimple("x@x.com", "Sub", "Body"));
    }

    @Test
    @DisplayName("sendAdminAlert delivers to the configured admin address")
    void sendAdminAlert_usesAdminAddress() {
        emailService.sendAdminAlert("Alert Subject", "Alert Body");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();
        assertArrayEquals(new String[]{ADMIN}, msg.getTo());
        assertEquals("Alert Subject", msg.getSubject());
        assertEquals("Alert Body", msg.getText());
    }

    @Test
    @DisplayName("sendAdminAlert also swallows MailException")
    void sendAdminAlert_mailException_doesNotPropagate() {
        doThrow(new MailSendException("SMTP down")).when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> emailService.sendAdminAlert("Subject", "Body"));
    }
}
