package ru.caselab.edm.backend.dto;

public record MinioSaveDto(String objectName, byte[] data) {
}
