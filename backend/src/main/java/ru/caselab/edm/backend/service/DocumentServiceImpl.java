package ru.caselab.edm.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.repository.DocumentRepository;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    @Override
    public Page<Document> getAllDocuments(int page, int size) {
        return documentRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public Document getDocument(long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Document not found"));
    }

    @Override
    public Document saveDocument(Document document) {
        return documentRepository.save(document);
    }

    @Override
    public Document updateDocument(long id, Document document) {
        Document existingDocument = documentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Document not found"));

        LocalDateTime now = LocalDateTime.now();

        //FIXME
        if (document.getDocumentTypeId() != null) {
            existingDocument.setDocumentTypeId(document.getDocumentTypeId());
        }
        if (document.getData() != null) {
            existingDocument.setData(document.getData().clone());
        }
        if (document.getCreationDate() != null) {
            existingDocument.setCreationDate(document.getCreationDate());
        }
        if (document.getUpdateDate() == null) {
            existingDocument.setUpdateDate(now);
        } else {
            existingDocument.setUpdateDate(document.getUpdateDate());
        }
        if (document.getUserId() != null) {
            existingDocument.setUserId(document.getUserId());
        }

        return documentRepository.save(existingDocument);
    }

    @Override
    public void deleteDocument(long id) {
        documentRepository.deleteById(id);
    }
}
