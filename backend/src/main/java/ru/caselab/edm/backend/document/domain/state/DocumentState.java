package ru.caselab.edm.backend.document.domain.state;

import ru.caselab.edm.backend.approval.domain.ApprovementProcessItem;
import ru.caselab.edm.backend.document.domain.DocumentVersion;

public interface DocumentState {
    void delete(DocumentVersion version);

    void modified(DocumentVersion version);

    void signAuthor(DocumentVersion version);

    void signContractor(ApprovementProcessItem item);

    void sendForSign(DocumentVersion version);

    void publishForVoting(DocumentVersion version);
}
