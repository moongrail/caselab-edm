package ru.caselab.edm.backend.dto.documentversion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import ru.caselab.edm.backend.dto.document.AttributeValuetoCreateDocumentDTO;
import ru.caselab.edm.backend.entity.DocumentAttributeValue;

import java.util.List;

@Data
public class DocumentVersionCreateDTO {
    @Schema(description = "Document name", example = "Spongebob best episodes")
    @NotBlank
    private String documentName;

    @Schema(description = "Content url", example = "")
    private String data;

    @Schema(description = "Id of the document whose version is the current document version", example = "1")
    private Long documentId;

    @Schema(description = "Value attributes", example = "[1, 2]")
    private List<DocumentAttributeValue> attributeValues;

    List<AttributeValuetoCreateDocumentDTO> attributesValuetoCreateDocumentDTO;
}
