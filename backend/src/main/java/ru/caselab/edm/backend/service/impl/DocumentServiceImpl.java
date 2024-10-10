package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.dto.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.DocumentUpdateDTO;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.exceptions.WrongDateException;
import ru.caselab.edm.backend.repository.AttributesRepository;
import ru.caselab.edm.backend.repository.DocumentRepository;
import ru.caselab.edm.backend.repository.DocumentTypeRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.DocumentService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentTypeRepository documentTypeRepository;

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
    public Document saveDocument(DocumentCreateDTO document) {
        Document newDocument = new Document();
        newDocument.setName(document.getName());
        newDocument.setDocumentType(
                documentTypeRepository.findById(document.getDocumentTypeId())
                        .orElseThrow(() -> new NoSuchElementException("Document type not found"))
                );
        newDocument.setUpdateDate(document.getUpdateDate());
        newDocument.setUser(
                userRepository.findById(document.getUserId())
                        .orElseThrow(() -> new NoSuchElementException("User not found"))
        );
        newDocument.setData(document.getData());
        newDocument.setCreationDate(document.getCreationDate());
        newDocument.setDocumentAttributeValues(new ArrayList<>());
        validateDate(newDocument);
        return documentRepository.save(newDocument);
    }

    @Transactional
    @Override
    public Document updateDocument(long id, DocumentUpdateDTO document) {
        Document existingDocument = documentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Document not found"));

        if(document.getCreationDate() != null) {
            if(document.getUpdateDate() != null) {
                existingDocument.setUpdateDate(document.getUpdateDate());
            } else {
                existingDocument.setUpdateDate(LocalDateTime.now());
            }
            existingDocument.setCreationDate(document.getCreationDate());
        }

        validateDate(existingDocument);

        if (document.getDocumentTypeId() != null) {
            existingDocument.setDocumentType(documentTypeRepository.findById(document.getDocumentTypeId())
                    .orElseThrow(() -> new NoSuchElementException("Document type not found")));
        }
        if (document.getData() != null) {
            existingDocument.setData(document.getData());
        }
        if (document.getUserId() != null) {
            existingDocument.setUser(userRepository.findById(document.getUserId())
                    .orElseThrow(() -> new NoSuchElementException("User not found")));
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
