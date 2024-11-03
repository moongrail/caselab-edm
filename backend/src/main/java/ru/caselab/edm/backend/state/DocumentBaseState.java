package ru.caselab.edm.backend.state;

import ru.caselab.edm.backend.entity.ApprovementProcessItem;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.exceptions.InvalidDocumentStateException;


public class DocumentBaseState implements DocumentState {
    @Override
    public void signAuthor(DocumentVersion version) {
        DocumentStatus status = version.getStatus();
        switch (status){
            case DRAFT -> version.setState(DocumentStatus.AUTHOR_SIGNED);
            default ->  throw new InvalidDocumentStateException("Action - sign by Author is not allowed in the current state.");
        }
    }

    @Override
    public void signContractor(ApprovementProcessItem item) {
        DocumentVersion version = item.getDocumentVersion();
        DocumentStatus status = version.getStatus();
        switch (status){
            case PENDING_CONTRACTOR_SIGN -> {
                switch (item.getStatus()){
                    case APPROVED -> version.setState(DocumentStatus.APPROVED);
                    case REJECTED -> version.setState(DocumentStatus.REJECTED);
                    case REWORK_REQUIRED -> version.setState(DocumentStatus.REWORK_REQUIRED);
                }
            }
            default ->  throw new InvalidDocumentStateException("Action - sign by Contractor is not allowed in the current state.");
        }
    }

    @Override
    public void sendForSign(DocumentVersion version) {
        DocumentStatus status = version.getStatus();
        switch (status){
            case DRAFT -> version.setState(DocumentStatus.PENDING_AUTHOR_SIGN);
            case AUTHOR_SIGNED -> version.setState(DocumentStatus.PENDING_CONTRACTOR_SIGN);
            default ->  throw new InvalidDocumentStateException("Action - send for sign is not allowed in the current state.");
        }
    }

    @Override
    public DocumentStatus getStatus(DocumentVersion version) {
        return version.getStatus();
    }

    @Override
    public void delete(DocumentVersion version) {
        DocumentStatus status = version.getStatus();
        switch (status){
            case DRAFT,REJECTED,APPROVED,REWORK_REQUIRED -> version.setState(DocumentStatus.DELETED);
            default ->  throw new InvalidDocumentStateException("Action - send for sign is not allowed in the current state.");
        }
    }

    @Override
    public void modified(DocumentVersion version) {
        DocumentStatus status = version.getStatus();
        switch (status){
            case DRAFT, REWORK_REQUIRED -> version.setState(DocumentStatus.REWORK_REQUIRED);
            default ->  throw new InvalidDocumentStateException("Action - modified is not allowed in the current state.");
        }
    }

}
