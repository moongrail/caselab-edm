package ru.caselab.edm.platform.mail;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SmtpEmailSenderTest {

    @Test
    void blankFrom_skipsSending() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        SmtpEmailSender sender = new SmtpEmailSender(mailSender, " ");

        sender.send(new EmailMessage("to@example.com", "subject", "<b>body</b>"));

        verify(mailSender, never()).createMimeMessage();
    }

    @Test
    void configuredFrom_sendsHtmlMessage() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(message);
        SmtpEmailSender sender = new SmtpEmailSender(mailSender, "from@example.com");

        sender.send(new EmailMessage("to@example.com", "subject", "<b>body</b>"));

        verify(mailSender).send(message);
        assertThat(message.getSubject()).isEqualTo("subject");
    }

    @Test
    void emailMessageExposesFields() {
        EmailMessage message = new EmailMessage("to@example.com", "subject", "<b>body</b>");

        assertThat(message.to()).isEqualTo("to@example.com");
        assertThat(message.subject()).isEqualTo("subject");
        assertThat(message.html()).contains("body");
    }

    @Test
    void autoConfigurationCreatesSender() {
        JavaMailSender mailSender = mock(JavaMailSender.class);

        EmailSender sender = new ru.caselab.edm.platform.autoconfigure.EmailAutoConfiguration()
                .emailSender(mailSender, "");

        sender.send(new EmailMessage("to@example.com", "subject", "body"));
        verify(mailSender, never()).createMimeMessage();
    }
}
