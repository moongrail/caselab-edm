package ru.caselab.edm.backend.dto.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.caselab.edm.backend.dto.attributevalue.DocumentAttributeValueUpdateDTO;
import ru.caselab.edm.backend.dto.file.FileDTO;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
public class DocumentUpdateDTO {

    @JsonProperty("name")
    @Schema(description = "Document name", example = "Spongebob best episodes")
    private String documentName;

    @JsonProperty("file")
    @Schema(description = "File's data")
    @Valid
    private FileDTO file;

    @Schema(description = "Value attributes")
    private List<DocumentAttributeValueUpdateDTO> attributeValues;
}
