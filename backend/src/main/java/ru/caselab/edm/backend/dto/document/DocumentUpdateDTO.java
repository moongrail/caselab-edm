package ru.caselab.edm.backend.dto.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.caselab.edm.backend.dto.attributevalue.DocumentAttributeValueUpdateDTO;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
public class DocumentUpdateDTO {

    @JsonProperty("name")
    @Schema(description = "Document name", example = "Spongebob best episodes")
    private String documentName;

    @JsonProperty("base64Data")
    @Schema(name = "base64Data", description = "File's data in Base64 format", example = "")
    private String data;

    @Schema(description = "Value attributes")
    private List<DocumentAttributeValueUpdateDTO> attributeValues;
}
