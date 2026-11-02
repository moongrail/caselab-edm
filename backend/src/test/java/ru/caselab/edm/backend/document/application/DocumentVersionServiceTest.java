package ru.caselab.edm.backend.document.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.caselab.edm.backend.document.api.dto.document.DocumentCreateDTO;
import ru.caselab.edm.backend.document.api.dto.file.FileDTO;
import ru.caselab.edm.platform.storage.ObjectStorage;
import ru.caselab.edm.platform.storage.StorageObject;
import ru.caselab.edm.backend.document.domain.Document;
import ru.caselab.edm.backend.document.domain.DocumentAttributeValue;
import ru.caselab.edm.backend.document.domain.DocumentVersion;
import ru.caselab.edm.backend.document.api.mapper.MinioDocumentMapper;
import ru.caselab.edm.backend.document.infrastructure.DocumentVersionRepository;
import ru.caselab.edm.backend.document.application.impl.DocumentVersionServiceImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DocumentVersionServiceTest {
    @Mock
    private DocumentVersionRepository documentVersionRepository;

    @Mock
    private ObjectStorage objectStorage;

    @Mock
    private DocumentAttributeValueService documentAttributeValueService;

    @Mock
    private MinioDocumentMapper minioDocumentMapper;

    @InjectMocks
    private DocumentVersionServiceImpl service;

    @Test
    void saveDocumentVersionTest() {
        UUID userId = UUID.randomUUID();


        DocumentCreateDTO documentCreateDTO = new DocumentCreateDTO();
        FileDTO fileDTO = new FileDTO("Data", "test_object_name");
        documentCreateDTO.setFile(fileDTO);
        documentCreateDTO.setDocumentName("test_document");

        Document newDocument = new Document();

        StorageObject minioSaveDto = new StorageObject("test_object_name", new byte[]{1, 2, 3, 4, 5}, "application/octet-stream");

        String data = Arrays.toString(new byte[]{1, 2, 3, 4, 5});

        when(minioDocumentMapper.map(any(DocumentCreateDTO.class), eq(userId))).thenReturn(minioSaveDto);
        when(documentAttributeValueService.createDocumentAttributeValues(any(), any(), any()))
                .thenReturn(Collections.singletonList(new DocumentAttributeValue()));

        DocumentVersion savedDocumentVersion = service.saveDocumentVersion(documentCreateDTO, newDocument, userId);

        assertNotNull(savedDocumentVersion);
        assertEquals(documentCreateDTO.getDocumentName(), savedDocumentVersion.getDocumentName());
        assertEquals(newDocument, savedDocumentVersion.getDocument());
        assertEquals(minioSaveDto.objectName(), savedDocumentVersion.getContentUrl());

        verify(objectStorage).put(minioSaveDto);
        verify(documentVersionRepository).save(savedDocumentVersion);
    }
}
