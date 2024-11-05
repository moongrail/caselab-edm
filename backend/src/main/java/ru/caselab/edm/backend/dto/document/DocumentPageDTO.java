package ru.caselab.edm.backend.dto.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "DTO for representing page of documents")
public class DocumentPageDTO {
    @JsonProperty("page")
    @Schema(description = "Page number", example = "1", minimum = "0")
    private int page;

    @JsonProperty("size")
    @Schema(description = "Number of elements per page", example = "10")
    private int size;

    @JsonProperty("totalPages")
    @Schema(description = "Total number of pages", example = "10")
    private int totalPages;

    @JsonProperty("totalElements")
    @Schema(description = "Total number of elements", example = "10")
    private int totalElements;

    @JsonProperty("content")
    @Schema(description = "List of documents")
    private List<DocumentDTO> content;
}
