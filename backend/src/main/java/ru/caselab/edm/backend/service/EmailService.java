package ru.caselab.edm.backend.service;

import ru.caselab.edm.backend.entity.Signature;

public interface EmailService {

    void sendEmailForSign(Signature signature);
}
