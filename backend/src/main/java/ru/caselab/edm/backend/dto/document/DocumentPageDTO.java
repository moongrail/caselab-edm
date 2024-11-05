package ru.caselab.edm.backend.dto.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
public class DocumentPageDTO {
    @JsonProperty("page")
    private int page;
    @JsonProperty("size")
    private int size;
    @JsonProperty("totalPages")
    private int totalPages;
    @JsonProperty("totalElements")
    private int totalElements;
    @JsonProperty("content")
    private List<DocumentDTO> content;
}
