package ru.caselab.edm.backend.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import java.util.List;

@Document(indexName = "attributes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttributeSearch {

    @Id
    private Long id;

    private String name;

    private String dataType;

    private boolean isRequired;

    List<Long> documents;
}
