package ru.caselab.edm.backend.document.application.helper;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import ru.caselab.edm.backend.document.exception.ContentTypeDetectionException;

import java.io.IOException;
import java.io.InputStream;

@Component
public class TikaInputStreamContentTypeDetector implements InputStreamContentTypeDetector {

    private final Tika tika = new Tika();

    @Override
    public String detect(String fileName, InputStream inputStream) {
        try {
            return tika.detect(inputStream, fileName);
        } catch (IOException ex) {
            throw new ContentTypeDetectionException("Exception while determining content type", ex);
        }
    }
}
