package ru.caselab.edm.backend.dto.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.caselab.edm.backend.dto.attributevalue.AttributeValuetoCreateDocumentDTO;
import ru.caselab.edm.backend.dto.file.FileDTO;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentCreateDTO {

    @JsonProperty("documentTypeId")
    @NotNull
    private Long documentTypeId;

    @JsonProperty("name")
    @Schema(description = "Document name", example = "Spongebob best episodes")
    @NotBlank
    @Size(min = 2, max = 255, message = "Document length must be between {min} and {max} characters")
    private String documentName;

    @JsonProperty("file")
    @Schema(description = "File's data")
    @NotNull
    private FileDTO file;

    @Schema(description = "Value attributes")
    private List<AttributeValuetoCreateDocumentDTO> attributeValues;
}
