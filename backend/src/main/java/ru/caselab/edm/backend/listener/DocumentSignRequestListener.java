package ru.caselab.edm.backend.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.caselab.edm.backend.event.DocumentSignRequestEvent;
import ru.caselab.edm.backend.service.EmailService;

@Component
public class DocumentSignRequestListener {

    private final EmailService emailService;

    @Autowired
    public DocumentSignRequestListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    @TransactionalEventListener
    public void onDocumentSentForSign(DocumentSignRequestEvent event) {
        emailService.sendEmailForSign(event.getSignature());
    }
}
