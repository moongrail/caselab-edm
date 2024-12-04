package ru.caselab.edm.backend.dto.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;


public record FileDTO(

        @JsonProperty("base64Data")
        @Schema(name = "base64Data", description = "File's data in Base64 format", example = "")
        @NotBlank
        String data,

        @JsonProperty("fileName")
        @Schema(name = "fileName", description = "File's name with extension", example = "document.docx")
        @NotBlank
        String fileName
) {
}
