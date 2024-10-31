package ru.caselab.edm.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
public class DocumentUpdateDTO {

/*    @JsonProperty("documentTypeId")
    @NotNull
    private Long documentTypeId;*/

    @JsonProperty("name")
    @Schema(description = "Document name", example = "Spongebob best episodes")
    private String documentName;

    @JsonProperty("data")
    @Schema(description = "Content url", example = "")
    private String data;

    @Schema(description = "Value attributes")
    private List<DocumentAttributeValueUpdateDTO> attributeValues;
}
