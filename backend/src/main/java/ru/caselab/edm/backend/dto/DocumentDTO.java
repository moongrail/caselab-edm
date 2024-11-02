package ru.caselab.edm.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Getter
@Setter
@NoArgsConstructor
public class DocumentDTO {

    @JsonProperty("Document id")
    private Long id;

    @JsonProperty("documentTypeId")
    private Long documentTypeId;

    @JsonProperty("userId")
    private UUID userId;

    @JsonProperty("createdAt")
    private Instant createdAt;
}
