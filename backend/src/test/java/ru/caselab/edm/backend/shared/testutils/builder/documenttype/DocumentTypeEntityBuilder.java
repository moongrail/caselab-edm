package ru.caselab.edm.backend.shared.testutils.builder.documenttype;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import ru.caselab.edm.backend.shared.testutils.builder.BaseBuilder;
import ru.caselab.edm.backend.document.domain.Attribute;
import ru.caselab.edm.backend.document.domain.Document;
import ru.caselab.edm.backend.document.domain.DocumentType;

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
