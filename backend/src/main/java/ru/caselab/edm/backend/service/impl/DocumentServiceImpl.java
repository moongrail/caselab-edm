package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.exceptions.WrongDateException;
import ru.caselab.edm.backend.repository.DocumentRepository;
import ru.caselab.edm.backend.service.DocumentService;

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

    @Transactional
    @Override
    public Document saveDocument(Document document) {
        validateDate(document);
        return documentRepository.save(document);
    }

    @Transactional
    @Override
    public Document updateDocument(long id, Document document) {
        Document existingDocument = documentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Document not found"));

        if(document.getCreationDate() != null) {
            if(document.getUpdateDate() != null) {
                validateDate(document);
                existingDocument.setUpdateDate(document.getUpdateDate());
            } else {
                existingDocument.setUpdateDate(LocalDateTime.now());
            }
            existingDocument.setCreationDate(document.getCreationDate());
        }

        if (document.getDocumentType() != null) {
            existingDocument.setDocumentType(document.getDocumentType());
        }
        if (document.getData() != null) {
            existingDocument.setData(document.getData());
        }
        if (document.getUser() != null) {
            existingDocument.setUser(document.getUser());
        }
        if(document.getName() != null) {
            existingDocument.setName(document.getName());
        }

        return documentRepository.save(existingDocument);
    }

    private void validateDate(Document document) {
        if (document.getCreationDate().isAfter(document.getUpdateDate())) {
            throw new WrongDateException("The creation date cannot be later than the update date");
        }
    }

    @Transactional
    @Override
    public void deleteDocument(long id) {
        documentRepository.deleteById(id);
    }
}
