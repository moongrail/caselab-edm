package ru.caselab.edm.backend.integration.email.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import ru.caselab.edm.backend.approval.domain.ApprovementProcessItem;
import ru.caselab.edm.backend.document.domain.Document;
import ru.caselab.edm.backend.document.domain.DocumentVersion;
import ru.caselab.edm.backend.document.domain.state.DocumentStatus;
import ru.caselab.edm.backend.shared.directory.UserDirectory;
import ru.caselab.edm.backend.shared.directory.UserSummary;
import ru.caselab.edm.platform.mail.EmailMessage;
import ru.caselab.edm.platform.mail.EmailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.DOCUMENT_ID;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.OWNER_ID;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.VERSION_ID;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.document;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.documentType;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.user;
import static ru.caselab.edm.backend.shared.testutils.fixtures.TestFixtures.version;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private EmailSender emailSender;
    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private UserDirectory userDirectory;

    private EmailServiceImpl emailService;
    private ApprovementProcessItem processItem;
    private UserSummary recipient;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(emailSender, templateEngine, userDirectory);
        recipient = user(OWNER_ID, "recipient");
        Document document = document(DOCUMENT_ID, OWNER_ID, documentType(1L));
        DocumentVersion documentVersion = version(VERSION_ID, document, DocumentStatus.DRAFT);
        documentVersion.setDocumentName("Contract");
        processItem = ApprovementProcessItem.builder()
                .signerId(OWNER_ID)
                .documentVersion(documentVersion)
                .build();
    }

    @Test
    void sendEmailForSign_buildsAndSendsHtmlMessage() {
        when(userDirectory.requireUser(OWNER_ID)).thenReturn(recipient);
        when(templateEngine.process(eq("email/sign"), any(Context.class)))
                .thenReturn("<html>sign</html>");

        emailService.sendEmailForSign(processItem);

        ArgumentCaptor<EmailMessage> messageCaptor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender).send(messageCaptor.capture());
        assertThat(messageCaptor.getValue().to()).isEqualTo(recipient.email());
        assertThat(messageCaptor.getValue().html()).isEqualTo("<html>sign</html>");
        verify(templateEngine).process(eq("email/sign"), any(Context.class));
    }
}
