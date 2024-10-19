package ru.caselab.edm.backend.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ru.caselab.edm.backend.entity.Signature;

@Getter
public class DocumentSignRequestEvent extends ApplicationEvent {

    private final Signature signature;

    public DocumentSignRequestEvent(Object source, Signature signature) {
        super(source);
        this.signature = signature;
    }
}
