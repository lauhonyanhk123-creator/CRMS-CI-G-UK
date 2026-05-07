package com.crms.service;

public interface EmailService {

    void sendSimple(String to, String subject, String text);

    void sendAdminAlert(String subject, String text);
}
