package ru.caselab.edm.backend.testutils.builder.documenttype;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import ru.caselab.edm.backend.testutils.builder.BaseBuilder;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentType;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor(staticName = "builder")
@With
public class DocumentTypeEntityBuilder implements BaseBuilder<DocumentType> {

    private Long id = null;
    private String name = "Default Name";
    private String description = "Default Description";
    private Instant createdAt = Instant.now();
    Instant updatedAt = Instant.now();

    private Set<Document> documents = new HashSet<>();
    private Set<Attribute> attributes = new HashSet<>();
    @Override
    public DocumentType build() {
        return new DocumentType(id, name, description, createdAt, updatedAt, documents, attributes);
    }

}
