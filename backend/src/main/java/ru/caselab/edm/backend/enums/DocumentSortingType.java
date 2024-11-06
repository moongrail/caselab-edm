package ru.caselab.edm.backend.enums;

import lombok.Getter;
import org.springframework.data.domain.Sort;

@Getter
public enum DocumentSortingType {
    WITHOUT(null, null),
    DOCUMENT_ID_ASC("id", Sort.Direction.ASC),
    DOCUMENT_ID_DESC("id", Sort.Direction.DESC),
    DOCUMENT_NAME_ASC("LOWER(documentName)", Sort.Direction.ASC),
    DOCUMENT_NAME_DESC("LOWER(documentName)", Sort.Direction.DESC),
    AUTHOR_ASC("LOWER(login)", Sort.Direction.ASC),
    AUTHOR_DESC("LOWER(login)", Sort.Direction.DESC),
    DATE_ASC("createdAt", Sort.Direction.ASC),
    DATE_DESC("createdAt", Sort.Direction.DESC),
    APPROVEMENT_PROCESS_STATUS_ASC("approvementProcessStatus", Sort.Direction.ASC),
    APPROVEMENT_PROCESS_STATUS_DESC("approvementProcessStatus", Sort.Direction.DESC);

    private final String fieldName;
    private final Sort.Direction direction;

    DocumentSortingType(String fieldName, Sort.Direction direction) {
        this.fieldName = fieldName;
        this.direction = direction;
    }
}
