package ru.caselab.edm.backend.integration.email.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import ru.caselab.edm.backend.approval.domain.ApprovementProcessItem;
import ru.caselab.edm.backend.integration.email.EmailService;
import ru.caselab.edm.backend.shared.directory.UserDirectory;
import ru.caselab.edm.backend.shared.directory.UserSummary;
import ru.caselab.edm.platform.mail.EmailMessage;
import ru.caselab.edm.platform.mail.EmailSender;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final EmailSender emailSender;
    private final TemplateEngine templateEngine;
    private final UserDirectory userDirectory;

    @Override
    public void sendEmailForSign(ApprovementProcessItem processItem) {
        UserSummary user = userDirectory.requireUser(processItem.getSignerId());
        log.info("Preparing email to send to user: {}", user.email());

        Context context = new Context();
        context.setVariable("firstName", user.firstName());
        context.setVariable("lastName", user.lastName());
        context.setVariable("documentName", processItem.getDocumentVersion().getDocumentName());

        String html = templateEngine.process("email/sign", context);
        emailSender.send(new EmailMessage(user.email(), "Подписание документа", html));
        log.info("Email sent successfully to user: {}", user.email());
    }
}
