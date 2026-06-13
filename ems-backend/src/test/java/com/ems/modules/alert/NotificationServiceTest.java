package com.ems.modules.alert;

import com.ems.modules.alert.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

public class NotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSendEmail() {
        notificationService.sendEmail("test@ems.local", "Subject", "Body Text");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
