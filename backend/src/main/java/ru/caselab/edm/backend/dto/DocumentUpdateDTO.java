package ru.caselab.edm.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class DocumentUpdateDTO {

    @JsonProperty("documentTypeId")
    private Long documentTypeId;

/*    @JsonProperty("userId")
    private UUID userId;*/

    @JsonProperty("createdAt")
    private LocalDateTime creationDate;

    @JsonProperty("name")
    private String documentName;

    @JsonProperty("data")
    private String data;
}
