package ru.caselab.edm.backend.mapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import ru.caselab.edm.backend.dto.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.DocumentDTO;
import ru.caselab.edm.backend.dto.DocumentPageDTO;
import ru.caselab.edm.backend.dto.DocumentUpdateDTO;
import ru.caselab.edm.backend.entity.Document;

import java.util.ArrayList;
import java.util.List;

@Component
public class DocumentMapperImpl implements DocumentMapper {
    @Override
    public Document toEntity(DocumentCreateDTO request) {
        Document document = new Document();
        document.setUserId(request.getUserId());
        document.setDocumentTypeId(request.getDocumentTypeId());
        document.setUpdateDate(request.getUpdateDate());
        document.setCreationDate(request.getCreationDate());
        document.setData(request.getData());
        return document;
    }

    @Override
    public Document toEntity(DocumentUpdateDTO request) {
        Document document = new Document();
        document.setUserId(request.getUserId());
        document.setDocumentTypeId(request.getDocumentTypeId());
        document.setUpdateDate(request.getUpdateDate());
        document.setCreationDate(request.getCreationDate());
        document.setData(request.getData());
        return document;
    }

    @Override
    public DocumentDTO toDto(Document entity) {
        DocumentDTO dto = new DocumentDTO();
        dto.setUserId(entity.getUserId());
        dto.setDocumentTypeId(entity.getDocumentTypeId());
        dto.setUpdateDate(entity.getUpdateDate());
        dto.setCreationDate(entity.getCreationDate());
        dto.setData(entity.getData());
        return dto;
    }

    @Override
    public DocumentPageDTO toDtoPage(Page<Document> requests) {
        DocumentPageDTO dto = new DocumentPageDTO();

        dto.setPage(requests.getNumber());
        dto.setSize(requests.getSize());
        dto.setTotalPages(requests.getTotalPages());
        dto.setTotalElements(requests.getNumberOfElements());
        dto.setContent(toDto(requests.getContent().stream().toList()));

        return dto;
    }

    private List<DocumentDTO> toDto(List<Document> entities) {
        return entities.stream().map(this::toDto).toList();
    }
}
