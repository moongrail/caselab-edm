package ru.caselab.edm.backend.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import ru.caselab.edm.backend.dto.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.DocumentDTO;
import ru.caselab.edm.backend.dto.DocumentPageDTO;
import ru.caselab.edm.backend.dto.DocumentUpdateDTO;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.repository.DocumentRepository;
import ru.caselab.edm.backend.service.DocumentTypeService;
import ru.caselab.edm.backend.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentMapperImpl implements DocumentMapper {

    private final UserService userService;
    private final DocumentTypeService documentTypeService;

    @Override
    public Document toEntity(DocumentCreateDTO request) {
        Document document = new Document();
        System.out.println("----------------- OK& --------------------");
        document.setUser(userService.getUserById(request.getUserId()));

        System.out.println("----------------- its OK --------------------");
        document.setDocumentType(
                documentTypeService.getDocumentTypeById(
                        request.getDocumentTypeId()
                )
        );
        System.out.println("----------------- MAY BE --------------------");
        document.setUpdateDate(request.getUpdateDate());
        document.setCreationDate(request.getCreationDate());
        document.setData(request.getData());
        document.setName(request.getName());
        System.out.println(document);
        return document;
    }

    @Override
    public Document toEntity(DocumentUpdateDTO request) {
        Document document = new Document();
        document.setUser(userService.getUserById(request.getUserId()));
        document.setDocumentType(
                documentTypeService.getDocumentTypeById(
                        request.getDocumentTypeId()
                )
        );
        document.setName(request.getName());
        document.setUpdateDate(request.getUpdateDate());
        document.setCreationDate(request.getCreationDate());
        document.setData(request.getData());
        return document;
    }

    @Override
    public DocumentDTO toDto(Document entity) {
        DocumentDTO dto = new DocumentDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setUserId(entity.getUser().getId());
        dto.setDocumentTypeId(entity.getDocumentType().getId());
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
