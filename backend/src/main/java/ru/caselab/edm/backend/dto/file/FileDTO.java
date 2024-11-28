package ru.caselab.edm.backend.dto.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;


public record FileDTO(

        @JsonProperty("base64Data")
        @Schema(name = "base64Data", description = "File's data in Base64 format", example = "")
        String data,

        @JsonProperty("fileName")
        @Schema(name = "fileName", description = "File's name with extension", example = "document.docx")
        String fileName
) {
}
