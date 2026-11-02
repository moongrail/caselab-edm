package ru.caselab.edm.backend.document.application.helper;

import java.io.InputStream;

public interface InputStreamContentTypeDetector {

    String detect(String fileName, InputStream inputStream);
}
