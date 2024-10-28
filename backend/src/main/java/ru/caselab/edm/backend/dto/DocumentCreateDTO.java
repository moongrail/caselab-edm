package ru.caselab.edm.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Data
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "DTO for create document and document version")
public class DocumentCreateDTO {

    @JsonProperty("documentTypeId")
    @NotNull
    @Schema(description = "Document type id", example = "1")
    private Long documentTypeId;

    @JsonProperty("userId")
    @NotNull
    @Schema(description = "User id", example = "0473e603-6144-4d1d-ba7b-e2fc475dcf89")
    private UUID userId;

    @Schema(description = "Attribute id", example = "1")
    private long attributeId;

    @JsonProperty("name")
    @NotBlank
    @Schema(description = "Document name", example = "Годовой отчёт")
    private String name;

    @JsonProperty("data")
    @NotNull
    @Schema(description = "File in base64", example = "c2Rmc2Fmc2FkZmdzYWRmZ3NhZGdmc2FkbGZhZGtqZ2prZGZzO2hqZ3NkO2ZqbmdqbnNuamdvO25zZGZvZ2hhZDtmZ2hvaWVyaGo=")
    private String data;
}
