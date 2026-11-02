package ru.caselab.edm.platform.storage;

/**
 * Binary object to store in external object storage.
 */
public record StorageObject(String objectName, byte[] data, String contentType) {
}
