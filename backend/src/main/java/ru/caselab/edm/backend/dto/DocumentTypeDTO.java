package ru.caselab.edm.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DocumentTypeDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createAt;
    private List<DocumentsAttributesDTO> attributes;
}
