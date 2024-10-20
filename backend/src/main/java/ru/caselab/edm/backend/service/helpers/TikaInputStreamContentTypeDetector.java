package ru.caselab.edm.backend.service.helpers;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import ru.caselab.edm.backend.exceptions.ContentTypeDetectionException;

import java.io.IOException;
import java.io.InputStream;

@Component
public class TikaInputStreamContentTypeDetector implements InputStreamContentTypeDetector {

    private final Tika tika = new Tika();

    @Override
    public String detect(InputStream inputStream) {
        try {
            return tika.detect(inputStream);
        } catch (IOException ex) {
            throw new ContentTypeDetectionException("Exception while determining content type", ex);
        }
    }
}
