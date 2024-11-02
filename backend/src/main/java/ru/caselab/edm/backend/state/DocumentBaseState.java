package ru.caselab.edm.backend.state;

import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.exceptions.InvalidDocumentStateException;

public class DocumentBaseState implements DocumentState {
    @Override
    public void signAuthor(DocumentVersion version) {

    }

    @Override
    public void signContractor(DocumentVersion version) {

    }

    @Override
    public void sendForSign(DocumentVersion version) {

    }

    @Override
    public DocumentStatus getStatus(DocumentVersion version) {
        throw new InvalidDocumentStateException("This action is not allowed in the current state.");
    }

    @Override
    public void delete(DocumentVersion version) {
        throw new InvalidDocumentStateException("Action - delete is not allowed in the current state.");
    }

    @Override
    public void modified(DocumentVersion version) {
        throw new InvalidDocumentStateException("Action - modified is not allowed in the current state.");
    }
}
