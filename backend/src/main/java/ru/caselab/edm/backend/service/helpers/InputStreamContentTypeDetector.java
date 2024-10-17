package ru.caselab.edm.backend.service.helpers;

import java.io.InputStream;

public interface InputStreamContentTypeDetector {

    String detect(InputStream inputStream);
}
