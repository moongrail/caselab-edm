package ru.caselab.edm.backend.service;

import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.entity.Document;

public interface DocumentService {
    Page<Document> getAllDocuments(int page, int size);
    Document getDocument(long id);
    Document saveDocument(Document document);
    Document updateDocument(long id, Document document);
    void deleteDocument(long id);
}
