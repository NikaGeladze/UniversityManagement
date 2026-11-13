package com.university.universitymanagement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.upload")
public record UploadProperties(
        String dir,
        long maxSizeBytes,
        List<String> allowedTypes
) {
}
