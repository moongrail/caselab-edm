package ru.caselab.edm.backend.utils;

import org.junit.jupiter.api.Test;

import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.caselab.edm.backend.utils.MinioObjectNameFormatterUtil.formatName;

public class MinioObjectNameFormatterUtilTest {

    private static final String SEPARATOR = "/";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");


    @Test
    void formatName_givenNameWithoutDate_shouldReturnFormattedObjectName() {
        UUID userId = UUID.randomUUID();
        String rawName = "document.pdf";

        String formattedName = formatName(userId, rawName);
        String decodedFormattedName = URLDecoder.decode(formattedName);

        assertThat(decodedFormattedName).doesNotContain("2023-10-21")
                .startsWith(userId + SEPARATOR);

        String[] parts = decodedFormattedName.split(SEPARATOR);
        assertThat(parts).hasSize(2);

    }


    @Test
    void formatName_givenNameWithDate_shouldReturnStringWithUpdatedDate() {
        UUID userId = UUID.randomUUID();
        String nameWithDate = "2023-10-20_14-30-00_myDocument.txt";
        String expectedPrefix = userId.toString() + SEPARATOR +
                LocalDateTime.now().format(FORMATTER);

        String formattedName = formatName(userId, nameWithDate);
        String decodedFormattedName = URLDecoder.decode(formattedName);

        assertThat(decodedFormattedName).doesNotContain("2023-10-21")
                .startsWith(expectedPrefix);
        String[] parts = decodedFormattedName.split(SEPARATOR);
        assertThat(parts).hasSize(2);
    }
}
