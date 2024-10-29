package ru.caselab.edm.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "DTO for update document")
public class DocumentUpdateDTO {

    @JsonProperty("documentTypeId")
    @Schema(description = "Document type id", example = "1")
    private Long documentTypeId;

    @JsonProperty("userId")
    @Schema(description = "User id", example = "0473e603-6144-4d1d-ba7b-e2fc475dcf89")
    private UUID userId;

    @JsonProperty("name")
    @Schema(description = "Document name", example = "Годовой отчёт")
    private String name;

    @JsonProperty("data")
    @Schema(description = "File in base64", example = "c2Rmc2Fmc2FkZmdzYWRmZ3NhZGdmc2FkbGZhZGtqZ2prZGZzO2hqZ3NkO2ZqbmdqbnNuamdvO25zZGZvZ2hhZDtmZ2hvaWVyaGo=")
    private String data;
}
