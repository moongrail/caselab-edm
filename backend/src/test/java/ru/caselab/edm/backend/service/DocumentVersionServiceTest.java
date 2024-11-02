package ru.caselab.edm.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.caselab.edm.backend.dto.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.MinioSaveDto;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentAttributeValue;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.mapper.MinioDocumentMapper;
import ru.caselab.edm.backend.repository.DocumentVersionRepository;
import ru.caselab.edm.backend.service.impl.DocumentVersionServiceImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DocumentVersionServiceTest {
    @Mock
    private DocumentVersionRepository documentVersionRepository;

    @Mock
    private MinioService minioService;

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
        documentCreateDTO.setDocumentName("test_document");

        Document newDocument = new Document();

        MinioSaveDto minioSaveDto = new MinioSaveDto("test_object_name", new byte[]{1, 2, 3, 4, 5});

        String data = Arrays.toString(new byte[]{1, 2, 3, 4, 5});

        when(minioDocumentMapper.map(documentCreateDTO, userId)).thenReturn(minioSaveDto);
        when(documentAttributeValueService.createDocumentAttributeValues(any(), any(), any()))
                .thenReturn(Collections.singletonList(new DocumentAttributeValue()));

        DocumentVersion savedDocumentVersion = service.saveDocumentVersion(documentCreateDTO, newDocument, userId);

        assertNotNull(savedDocumentVersion);
        assertEquals(documentCreateDTO.getDocumentName(), savedDocumentVersion.getDocumentName());
        assertEquals(newDocument, savedDocumentVersion.getDocument());
        assertEquals(minioSaveDto.objectName(), savedDocumentVersion.getContentUrl());

        verify(minioService).saveObject(minioSaveDto);
        verify(documentVersionRepository).save(savedDocumentVersion);
    }
}
