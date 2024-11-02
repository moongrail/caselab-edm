package ru.caselab.edm.backend.state;

import ru.caselab.edm.backend.entity.DocumentVersion;

public class DraftDocumentState extends DocumentBaseState {
    @Override
    public DocumentStatus getStatus(DocumentVersion version) {
        return DocumentStatus.DRAFT;
    }

    @Override
    public void signAuthor(DocumentVersion version) {
        version.setState(DocumentStatus.AUTHOR_SIGNED);
    }

    @Override
    public void modified(DocumentVersion version) {
        version.setState(DocumentStatus.DRAFT);
    }
}
