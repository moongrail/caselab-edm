package ru.caselab.edm.backend.dto.minio;

public record MinioSaveDto(String objectName, byte[] data) {
}
