package ru.caselab.edm.backend.approval.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ru.caselab.edm.backend.approval.domain.ApprovementProcessItem;

@Getter
public class DocumentSignRequestEvent extends ApplicationEvent {

    private final ApprovementProcessItem approvementProcessItem;

    public DocumentSignRequestEvent(Object source, ApprovementProcessItem approvementProcessItem) {
        super(source);
        this.approvementProcessItem = approvementProcessItem;
    }
}
