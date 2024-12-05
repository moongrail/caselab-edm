package ru.caselab.edm.backend.dto.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.caselab.edm.backend.dto.attributevalue.AttributeValuetoCreateDocumentDTO;
import ru.caselab.edm.backend.dto.file.FileDTO;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
public class DocumentCreateDTO {

    @JsonProperty("documentTypeId")
    @NotNull
    private Long documentTypeId;

    @JsonProperty("name")
    @Schema(description = "Document name", example = "Spongebob best episodes")
    @NotBlank
    private String documentName;

    @JsonProperty("file")
    @Schema(description = "File's data")
    @NotNull
    @Valid
    private FileDTO file;

    @Schema(description = "Value attributes")
    private List<AttributeValuetoCreateDocumentDTO> attributeValues;
}
