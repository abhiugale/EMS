package com.ems.modules.alert.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;

    @Value("${twilio.sid:}")
    private String twilioSid;

    @Value("${twilio.authToken:}")
    private String twilioAuthToken;

    @Value("${twilio.from:}")
    private String twilioFrom;

    @Value("${whatsapp.api.token:}")
    private String whatsappToken;

    @Value("${whatsapp.phone.number.id:}")
    private String whatsappPhoneNumberId;

    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email notification successfully sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email notification to: {}. Error: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendSms(String to, String body) {
        try {
            if (twilioSid == null || twilioSid.isEmpty() || twilioAuthToken == null || twilioAuthToken.isEmpty()) {
                log.warn("Twilio credentials not configured. Skipping SMS notification to: {}", to);
                return;
            }
            log.info("SMS SENT (MOCK/PROD STUB): To={}, Msg={}", to, body);
        } catch (Exception e) {
            log.error("Failed to send SMS notification to: {}. Error: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendWhatsApp(String to, String body) {
        try {
            if (whatsappToken == null || whatsappToken.isEmpty() || whatsappPhoneNumberId == null || whatsappPhoneNumberId.isEmpty()) {
                log.warn("WhatsApp API credentials not configured. Skipping WhatsApp notification to: {}", to);
                return;
            }
            log.info("WHATSAPP SENT (MOCK/PROD STUB): To={}, Msg={}", to, body);
        } catch (Exception e) {
            log.error("Failed to send WhatsApp notification to: {}. Error: {}", to, e.getMessage());
        }
    }
}
