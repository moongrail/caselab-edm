package ru.caselab.edm.backend.utils;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MinioObjectNameFormatterUtil {

    private static final String SEPARATOR = "/";
    private static final String DELIMITER = "_";
    private static final String DATE_TIME_PATTERN = "\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public static String formatName(UUID userId, String rawName) {
        String nameWithoutDate = removeExistingDatePrefix(rawName);
        String currentTime = getCurrentTimeAsPrefix();

        String objectName = concatString(userId.toString(), currentTime, nameWithoutDate);
        try {
            String encodedObjectName = URLEncoder.encode(objectName, "UTF-8");
            return encodedObjectName.replace("%", "_PERCENT_");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }

    private static String removeExistingDatePrefix(String name) {
        return name.replaceFirst(DATE_TIME_PATTERN, "");
    }

    private static String getCurrentTimeAsPrefix() {
        return FORMATTER.format(LocalDateTime.now());
    }

    private static String concatString(String userId, String currentTime, String name) {
        return userId +
                SEPARATOR +
                currentTime +
                DELIMITER +
                name;
    }
}
