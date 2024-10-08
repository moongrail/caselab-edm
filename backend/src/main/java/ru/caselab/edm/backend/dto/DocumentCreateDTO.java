package ru.caselab.edm.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Getter
@Setter
@NoArgsConstructor
public class DocumentCreateDTO {

    @JsonProperty("documentTypeId")
    @NotNull
    private Long documentTypeId;

    @JsonProperty("userId")
    @NotNull
    private UUID userId;

    @JsonProperty("name")
    @NotNull
    private String name;

    @JsonProperty("createdAt")
    private LocalDateTime creationDate;

    @JsonProperty("updatedAt")
    private LocalDateTime updateDate;

    @JsonProperty("data")
    @NotNull
    private byte[] data;
}
