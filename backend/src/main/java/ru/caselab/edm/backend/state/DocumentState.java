package ru.caselab.edm.backend.state;

import ru.caselab.edm.backend.entity.ApprovementProcessItem;
import ru.caselab.edm.backend.entity.DocumentVersion;

public interface DocumentState {
    void delete(DocumentVersion version);
    void modified(DocumentVersion version);
    void signAuthor(DocumentVersion version);
    void signContractor(ApprovementProcessItem item);
    void sendForSign(DocumentVersion version);
    void publishForVoting(DocumentVersion version);
}
