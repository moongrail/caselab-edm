package ru.caselab.edm.backend.dto;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class DocumentTypeDTO {
    private Long id;
    private String name;
    private String description;
    private Set<Long> attributeIds;
}
