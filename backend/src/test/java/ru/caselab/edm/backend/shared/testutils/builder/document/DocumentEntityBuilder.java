package ru.caselab.edm.backend.shared.testutils.builder.document;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import ru.caselab.edm.backend.document.domain.Document;
import ru.caselab.edm.backend.document.domain.DocumentType;
import ru.caselab.edm.backend.document.domain.DocumentVersion;
import ru.caselab.edm.backend.shared.testutils.builder.BaseBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor(staticName = "builder")
@With
public class DocumentEntityBuilder implements BaseBuilder<Document> {

    private Long id = null;
    private UUID ownerId;
    private DocumentType documentType;
    private Instant createdAt = Instant.now();
    private List<DocumentVersion> documentVersion = new ArrayList<>();

    @Override
    public Document build() {
        Document document = new Document();

        document.setId(id);
        document.setOwnerId(ownerId);
        document.setDocumentType(documentType);
        document.setCreatedAt(createdAt);
        document.setDocumentVersion(documentVersion);

        return document;
    }
}
