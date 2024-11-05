package ru.caselab.edm.backend.dto.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
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
