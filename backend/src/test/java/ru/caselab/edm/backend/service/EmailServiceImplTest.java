package ru.caselab.edm.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.service.impl.EmailServiceImpl;
import ru.caselab.edm.backend.state.DocumentStatus;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.DOCUMENT_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.OWNER_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.VERSION_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.document;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.documentType;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.user;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.version;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private MimeMessage message;

    private EmailServiceImpl emailService;
    private ApprovementProcessItem processItem;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender, templateEngine);
        ReflectionTestUtils.setField(emailService, "senderEmail", "sender@example.com");

        User recipient = user(OWNER_ID, "recipient");
        recipient.setEmail("recipient@example.com");
        recipient.setFirstName("Ivan");
        recipient.setLastName("Ivanov");
        Document document = document(DOCUMENT_ID, recipient, documentType(1L));
        DocumentVersion documentVersion = version(VERSION_ID, document, DocumentStatus.DRAFT);
        documentVersion.setDocumentName("Contract");
        processItem = ApprovementProcessItem.builder()
                .user(recipient)
                .documentVersion(documentVersion)
                .build();
    }

    @Test
    void sendEmailForSign_withoutConfiguredSender_skipsNotification() {
        EmailServiceImpl unconfiguredService = new EmailServiceImpl(mailSender, templateEngine);

        unconfiguredService.sendEmailForSign(processItem);

        verifyNoInteractions(mailSender, templateEngine);
    }

    @Test
    void sendEmailForSign_buildsAndSendsHtmlMessage() {
        when(mailSender.createMimeMessage()).thenReturn(message);
        when(templateEngine.process(eq("email/sign"), any(Context.class)))
                .thenReturn("<html>sign</html>");

        emailService.sendEmailForSign(processItem);

        verify(mailSender).send(message);
        verify(templateEngine).process(eq("email/sign"), any(Context.class));
    }

    @Test
    void sendEmailForSign_messagingFailure_isLoggedAndSuppressed() throws MessagingException {
        when(mailSender.createMimeMessage()).thenReturn(message);
        when(templateEngine.process(eq("email/sign"), any(Context.class)))
                .thenReturn("<html>sign</html>");
        doThrow(new MessagingException("smtp failure"))
                .when(message).setHeader(anyString(), anyString());

        emailService.sendEmailForSign(processItem);

        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
