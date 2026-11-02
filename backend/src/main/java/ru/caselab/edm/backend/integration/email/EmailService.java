package ru.caselab.edm.backend.integration.email;

import ru.caselab.edm.backend.approval.domain.ApprovementProcessItem;

public interface EmailService {
    void sendEmailForSign(ApprovementProcessItem processItem);
}
