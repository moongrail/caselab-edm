package ru.caselab.edm.platform.storage;

/**
 * Service-neutral object storage port.
 */
public interface ObjectStorage {

    void put(StorageObject object);

    void delete(String objectName);

    String temporaryUrl(String objectName);
}
