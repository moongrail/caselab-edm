package ru.caselab.edm.backend.testutils.builder.document;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import ru.caselab.edm.backend.testutils.builder.BaseBuilder;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentType;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.entity.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor(staticName = "builder")
@With
public class DocumentEntityBuilder implements BaseBuilder<Document> {

    private Long id = null;
    private User user;
    private DocumentType documentType;
    private Instant createdAt = Instant.now();
    private List<DocumentVersion> documentVersion = new ArrayList<>();

    @Override
    public Document build() {
        Document document = new Document();

        document.setId(id);
        document.setUser(user);
        document.setDocumentType(documentType);
        document.setCreatedAt(createdAt);
        document.setDocumentVersion(documentVersion);

        return document;
    }
}
