package ru.vibeart.email.services;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

// Подключает mockito в JUnit
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    // Заменяет внешние зависимости
    @Mock
    JavaMailSender mailSender;

    EmailService emailService;

    MimeMessage realMessage;

    @BeforeEach
    void setUp() {
        String fromEmail = System.getenv().getOrDefault("EMAIL_FROM", "test-sender@mail.local");
        emailService = new EmailService(mailSender, fromEmail);

        // Создание пустого письма без подключения к серверу (вместо SMTP-сессии null)
        realMessage = new jakarta.mail.internet.MimeMessage((jakarta.mail.Session) null);
        when(mailSender.createMimeMessage()).thenReturn(realMessage);
    }

    @Test
    void sendCustomVerificationEmail_shouldSendEmail() {
        emailService.sendCustomVerificationEmail("user@example.com", "123456");
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(realMessage);
    }

    @Test
    void sendCustomPasswordEmail_shouldSendEmail() {
        emailService.sendCustomPasswordEmail("user@example.com", "123456789");
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(realMessage);
    }

    @Test
    void sendCustomVerificationEmail_shouldHandleExceptionGracefully() {
        doThrow(new RuntimeException("Simulated failure"))
                .when(mailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() ->
                emailService.sendCustomVerificationEmail("user@example.com", "123456"));

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }
}
