package ru.caselab.edm.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import ru.caselab.edm.backend.dto.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.DocumentUpdateDTO;
import ru.caselab.edm.backend.dto.MinioSaveDto;
import ru.caselab.edm.backend.utils.MinioObjectNameFormatterUtil;

import java.util.Base64;
import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MinioDocumentMapper {

    @Mapping(source = "data", target = "data", qualifiedByName = "getByteArrayFromBase64")
    @Mapping(target = "objectName", expression = "java(formatObjectName(dto.getUserId(), dto.getName()))")
    MinioSaveDto map(DocumentCreateDTO dto);

    @Mapping(source = "data", target = "data", qualifiedByName = "getByteArrayFromBase64")
    @Mapping(target = "objectName", expression = "java(formatObjectName(dto.getUserId(), dto.getName()))")
    MinioSaveDto map(DocumentUpdateDTO dto);


    @Named("getByteArrayFromBase64")
    default byte[] getByteArrayFromBase64(String base64data) {
        return Base64.getDecoder().decode(base64data);
    }

    default String formatObjectName(UUID userId, String documentName) {
        return MinioObjectNameFormatterUtil.formatName(userId, documentName);
    }
}
