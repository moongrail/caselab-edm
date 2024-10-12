package ru.caselab.edm.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @JsonProperty("name")
    private String name;
}
