package ru.caselab.edm.backend.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;

@Getter
public class DocumentSignRequestEvent extends ApplicationEvent {

    private final ApprovementProcessItem approvementProcessItem;

    public DocumentSignRequestEvent(Object source, ApprovementProcessItem approvementProcessItem) {
        super(source);
        this.approvementProcessItem = approvementProcessItem;
    }
}
