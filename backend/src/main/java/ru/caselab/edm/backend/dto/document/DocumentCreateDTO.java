package ru.caselab.edm.backend.dto.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.caselab.edm.backend.dto.attributevalue.AttributeValuetoCreateDocumentDTO;

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

    @JsonProperty("base64Data")
    @Schema(description = "File's data in Base64 format", example = "")
    private String data;

    @Schema(description = "Value attributes")
    private List<AttributeValuetoCreateDocumentDTO> attributeValues;
}
