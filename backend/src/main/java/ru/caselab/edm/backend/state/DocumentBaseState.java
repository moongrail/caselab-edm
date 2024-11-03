package ru.caselab.edm.backend.state;

import ru.caselab.edm.backend.entity.ApprovementProcess;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.exceptions.InvalidDocumentStateException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static ru.caselab.edm.backend.enums.ApprovementProcessStatus.PUBLISHED_FOR_VOTING;


public class DocumentBaseState implements DocumentState {

    private final Map<DocumentStatus, Set<DocumentStatus>> allowedTransitions = new EnumMap<>(DocumentStatus.class);

    public DocumentBaseState() {
        allowedTransitions.put(DocumentStatus.DRAFT, Set.of(DocumentStatus.AUTHOR_SIGNED, DocumentStatus.PENDING_AUTHOR_SIGN, DocumentStatus.IN_VOTING, DocumentStatus.DELETED,DocumentStatus.REWORK_REQUIRED));
        allowedTransitions.put(DocumentStatus.PENDING_CONTRACTOR_SIGN, Set.of(DocumentStatus.APPROVED, DocumentStatus.REJECTED, DocumentStatus.REWORK_REQUIRED));
        allowedTransitions.put(DocumentStatus.AUTHOR_SIGNED, Set.of(DocumentStatus.PENDING_CONTRACTOR_SIGN));
        allowedTransitions.put(DocumentStatus.REWORK_REQUIRED, Set.of(DocumentStatus.DELETED,DocumentStatus.REWORK_REQUIRED));
        allowedTransitions.put(DocumentStatus.APPROVED, Set.of(DocumentStatus.DELETED));
        allowedTransitions.put(DocumentStatus.REJECTED, Set.of(DocumentStatus.DELETED));
        allowedTransitions.put(DocumentStatus.PENDING_AUTHOR_SIGN, Set.of(DocumentStatus.AUTHOR_SIGNED));
    }


    private void changeState(DocumentVersion version, DocumentStatus newStatus) {
        DocumentStatus currentStatus = version.getStatus();
        if (allowedTransitions.getOrDefault(currentStatus, Set.of()).contains(newStatus)) {
            version.setState(newStatus);
        } else {
            throw new InvalidDocumentStateException("Transition from " + currentStatus + " to " + newStatus + " is not allowed.");
        }
    }



    @Override
    public void signAuthor(DocumentVersion version) {
        if(isVotingDocument(version)){
            ApprovementProcess process = version.getApprovementProcesses().get(0);
            if (process.getStatus()!=PUBLISHED_FOR_VOTING){
                throw new InvalidDocumentStateException("Action - sign document during the voting is not allowed in the current state.");
            }
        }else {
            changeState(version, DocumentStatus.AUTHOR_SIGNED);
        }
    }

    @Override
    public void signContractor(ApprovementProcessItem item) {
        ApprovementProcess process = item.getApprovementProcess();
        if(process!=null){
            if (process.getStatus()!=PUBLISHED_FOR_VOTING){
                 throw new InvalidDocumentStateException("Action - sign document during the voting is not allowed in the current state.");
            }
        } else {
            DocumentStatus targetStatus;
                    switch (item.getStatus()) {
                        case APPROVED -> targetStatus = DocumentStatus.APPROVED;
                        case REJECTED -> targetStatus = DocumentStatus.REJECTED;
                        case REWORK_REQUIRED -> targetStatus = DocumentStatus.REWORK_REQUIRED;
                        default -> throw new InvalidDocumentStateException("Invalid status for contractor sign action.");
                    }
            changeState(item.getDocumentVersion(), targetStatus);
                }

        }


    @Override
    public void sendForSign(DocumentVersion version) {
        DocumentStatus newStatus = (version.getStatus() == DocumentStatus.DRAFT)
                ? DocumentStatus.PENDING_AUTHOR_SIGN
                : DocumentStatus.PENDING_CONTRACTOR_SIGN;
        changeState(version, newStatus);
    }

    @Override
    public void publishForVoting(DocumentVersion version) {
        changeState(version,DocumentStatus.IN_VOTING);

    }

    private boolean isVotingDocument(DocumentVersion version) {
        return version.getApprovementProcesses()!=null && !version.getApprovementProcesses().isEmpty();
    }


    @Override
    public void delete(DocumentVersion version) {
        changeState(version, DocumentStatus.DELETED);
    }

    @Override
    public void modified(DocumentVersion version) {
        changeState(version, DocumentStatus.REWORK_REQUIRED);
    }

}
