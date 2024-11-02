package ru.caselab.edm.backend.state;

import ru.caselab.edm.backend.entity.DocumentVersion;

public interface DocumentState {
    void delete(DocumentVersion version);
    void modified(DocumentVersion version);
    void signAuthor(DocumentVersion version);
    void signContractor(DocumentVersion version);
    void sendForSign(DocumentVersion version);
    DocumentStatus getStatus(DocumentVersion version);
}
