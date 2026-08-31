package com.learning.service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class MailService {
    public void sendPasswordReset(String recipient, String resetLink) throws Exception {
        String host = required("MAIL_HOST");
        String from = required("MAIL_FROM");
        String username = System.getenv("MAIL_USERNAME");
        String password = System.getenv("MAIL_PASSWORD");
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", valueOrDefault("MAIL_PORT", "587"));
        properties.put("mail.smtp.auth", Boolean.toString(username != null && !username.isBlank()));
        properties.put("mail.smtp.starttls.enable", valueOrDefault("MAIL_STARTTLS", "true"));
        Session session = Session.getInstance(properties, username == null || username.isBlank() ? null : new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient, false));
        message.setSubject("Password reset request");
        message.setText("Use this link to reset your password. It expires in 15 minutes and can be used once:\n\n" + resetLink);
        Transport.send(message);
    }

    private String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalStateException("Missing environment variable " + name);
        return value;
    }

    private String valueOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
