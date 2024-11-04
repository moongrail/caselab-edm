package ru.caselab.edm.backend.service;

import ru.caselab.edm.backend.entity.ApprovementProcessItem;

public interface EmailService {
    void sendEmailForSign(ApprovementProcessItem processItem);
}
