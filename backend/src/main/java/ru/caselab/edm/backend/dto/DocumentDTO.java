package ru.caselab.edm.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Data
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "DTO for representing document")
public class DocumentDTO {

    @JsonProperty("id")
    @Schema(description = "Document id", example = "1")
    private Long id;

    @JsonProperty("documentTypeId")
    @Schema(description = "Document type id", example = "1")
    private Long documentTypeId;

    @JsonProperty("userId")
    @Schema(description = "User id", example = "0473e603-6144-4d1d-ba7b-e2fc475dcf89")
    private UUID userId;
}
