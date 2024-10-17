package ru.caselab.edm.backend.service;

import ru.caselab.edm.backend.dto.MinioSaveDto;

public interface MinioService {

    void saveObject(MinioSaveDto minioSaveDto);
    void deleteByObjectName(String objectName);
    String generateTemporaryUrlToObject(String objectName);
}
