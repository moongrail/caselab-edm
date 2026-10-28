package ru.caselab.edm.backend.mapper.attribute;

import org.mapstruct.Mapper;
import ru.caselab.edm.backend.dto.attribute.AttributeDTO;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.DocumentType;

import java.util.LinkedHashSet;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface AttributeMapper {

    default AttributeDTO toDTO(Attribute attribute) {
        if (attribute == null) {
            return null;
        }
        AttributeDTO dto = new AttributeDTO();
        dto.setId(attribute.getId());
        dto.setName(attribute.getName());
        dto.setDataType(attribute.getDataType());
        dto.setRequired(attribute.isRequired());
        dto.setDocumentTypeIds(mapDocumentTypes(attribute.getDocumentTypes()));
        return dto;
    }

    default Attribute toEntity(AttributeDTO attributeDTO) {
        if (attributeDTO == null) {
            return null;
        }
        Attribute attribute = new Attribute();
        attribute.setId(attributeDTO.getId());
        attribute.setName(attributeDTO.getName());
        attribute.setDataType(attributeDTO.getDataType());
        attribute.setRequired(attributeDTO.isRequired());
        attribute.setDocumentTypes(mapDocumentTypeIds(attributeDTO.getDocumentTypeIds()));
        return attribute;
    }

    default Long mapDocumentTypeToId(DocumentType documentType) {
        return documentType != null ? documentType.getId() : null;
    }

    default DocumentType mapIdToDocumentType(Long id) {
        if (id == null) {
            return null;
        }
        DocumentType documentType = new DocumentType();
        documentType.setId(id);
        return documentType;
    }

    private Set<Long> mapDocumentTypes(Set<DocumentType> documentTypes) {
        if (documentTypes == null) {
            return null;
        }
        Set<Long> ids = new LinkedHashSet<>();
        for (DocumentType documentType : documentTypes) {
            Long id = mapDocumentTypeToId(documentType);
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    private Set<DocumentType> mapDocumentTypeIds(Set<Long> ids) {
        if (ids == null) {
            return null;
        }
        Set<DocumentType> documentTypes = new LinkedHashSet<>();
        for (Long id : ids) {
            DocumentType documentType = mapIdToDocumentType(id);
            if (documentType != null) {
                documentTypes.add(documentType);
            }
        }
        return documentTypes;
    }
}
