package ru.caselab.edm.backend.dto.documentversion;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import ru.caselab.edm.backend.dto.attributevalue.AttributeValuetoCreateDocumentDTO;
import ru.caselab.edm.backend.dto.file.FileDTO;
import ru.caselab.edm.backend.entity.DocumentAttributeValue;

import java.util.List;

@Data
public class DocumentVersionCreateDTO {
    @Schema(description = "Document name", example = "Spongebob best episodes")
    @NotBlank
    private String documentName;

    @JsonProperty("file")
    @Schema(name = "File's data", description = "File's data")
    private FileDTO file;

    @Schema(description = "Id of the document whose version is the current document version", example = "1")
    private Long documentId;

    @Schema(description = "Value attributes", example = "[1, 2]")
    private List<DocumentAttributeValue> attributeValues;

    List<AttributeValuetoCreateDocumentDTO> attributesValuetoCreateDocumentDTO;
}
