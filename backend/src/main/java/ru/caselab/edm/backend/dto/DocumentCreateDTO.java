package ru.caselab.edm.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Getter
@Setter
@NoArgsConstructor
public class DocumentCreateDTO {

    @JsonProperty("documentTypeId")
    @NotBlank
    private Long documentTypeId;

    @JsonProperty("userId")
    @NotBlank
    private UUID userId;

    @JsonProperty("createdAt")
    @NotBlank
    private LocalDateTime creationDate;

    @JsonProperty("updatedAt")
    @NotBlank
    private LocalDateTime updateDate;

    @JsonProperty("data")
    @NotBlank
    private byte[] data;
}
