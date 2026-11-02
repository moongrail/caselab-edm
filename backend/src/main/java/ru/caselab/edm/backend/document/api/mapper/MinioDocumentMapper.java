package ru.caselab.edm.backend.document.api.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.caselab.edm.backend.document.api.dto.document.DocumentCreateDTO;
import ru.caselab.edm.backend.document.application.helper.InputStreamContentTypeDetector;
import ru.caselab.edm.backend.integration.minio.util.MinioObjectNameFormatterUtil;
import ru.caselab.edm.platform.storage.StorageObject;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.UUID;

/**
 * Converts an API file payload into a storage object with a generated object
 * name and a detected content type.
 */
@Component
@RequiredArgsConstructor
public class MinioDocumentMapper {

    private final InputStreamContentTypeDetector contentTypeDetector;

    public StorageObject map(DocumentCreateDTO dto, UUID userId) {
        return map(dto.getFile().fileName(), dto.getFile().data(), userId);
    }

    public StorageObject map(String documentName, String data, UUID userId) {
        byte[] bytes = Base64.getDecoder().decode(data);
        String objectName = MinioObjectNameFormatterUtil.formatName(userId, documentName);
        String contentType = contentTypeDetector.detect(documentName,
                new ByteArrayInputStream(bytes));
        return new StorageObject(objectName, bytes, contentType);
    }
}
