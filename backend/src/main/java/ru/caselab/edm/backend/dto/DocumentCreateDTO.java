package ru.caselab.edm.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class DocumentCreateDTO {

    @JsonProperty("documentTypeId")
    @NotNull
    private Long documentTypeId;

    @JsonProperty("userId")
    @NotNull
    private UUID userId;

    private long attributeId;

    @JsonProperty("name")
    @NotBlank
    private String name;

    @JsonProperty("data")
    @NotNull
    private String data;
}
