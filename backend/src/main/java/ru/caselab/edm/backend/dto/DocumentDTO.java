package ru.caselab.edm.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
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
public class DocumentDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("documentTypeId")
    private Long documentTypeId;

    @JsonProperty("userId")
    private UUID userId;

    @JsonProperty("createdAt")
    private LocalDateTime creationDate;

    @JsonProperty("updatedAt")
    private LocalDateTime updateDate;

    @JsonProperty("data")
    private byte[] data;
}
