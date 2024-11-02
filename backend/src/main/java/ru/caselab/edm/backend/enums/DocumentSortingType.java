package ru.caselab.edm.backend.enums;

import lombok.Data;
import lombok.Getter;
import org.springframework.data.domain.Sort;

@Getter
public enum DocumentSortingType {
    WITHOUT(null, null),
    DOCUMENT_NAME_ASC("documentName", Sort.Direction.ASC),
    DOCUMENT_NAME_DESC("documentName", Sort.Direction.DESC),
    STATUS_ASC("status", Sort.Direction.ASC),
    STATUS_DESC("status", Sort.Direction.DESC),
    AUTHOR_ASC("lastName", Sort.Direction.ASC),
    AUTHOR_DESC("lastName", Sort.Direction.DESC),
    DATE_ASC("createdAt", Sort.Direction.ASC),
    DATE_DESC("createdAt", Sort.Direction.DESC);

    private String fieldName;
    private Sort.Direction direction;

    DocumentSortingType(String fieldName, Sort.Direction direction) {
        this.fieldName = fieldName;
        this.direction = direction;
    }
}
