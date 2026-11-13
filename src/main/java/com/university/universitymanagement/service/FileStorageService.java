package com.university.universitymanagement.service;

import com.university.universitymanagement.config.UploadProperties;
import com.university.universitymanagement.exception.FileStorageException;
import com.university.universitymanagement.exception.InvalidFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private static final Map<String, String> EXTENSIONS_BY_TYPE = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private final UploadProperties properties;
    private final Path rootDir;

    public FileStorageService(UploadProperties properties) {
        this.properties = properties;
        this.rootDir = Path.of(properties.dir()).toAbsolutePath().normalize();
    }

    /**
     * Stores the file under {@code <upload-dir>/<subDirectory>} and returns its path relative to the
     * application working directory, using forward slashes.
     */
    public String store(MultipartFile file, String subDirectory) {
        String contentType = validate(file);
        String filename = UUID.randomUUID() + EXTENSIONS_BY_TYPE.get(contentType);

        Path targetDir = rootDir.resolve(subDirectory).normalize();
        Path target = targetDir.resolve(filename);
        if (!target.startsWith(rootDir)) {
            throw new InvalidFileException("Invalid storage location");
        }

        try (InputStream in = file.getInputStream()) {
            Files.createDirectories(targetDir);
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileStorageException("Could not store file", e);
        }

        return properties.dir() + "/" + subDirectory + "/" + filename;
    }

    public void delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        Path path = Path.of(relativePath).toAbsolutePath().normalize();
        if (!path.startsWith(rootDir)) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Could not delete file {}", path, e);
        }
    }

    private String validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File must not be empty");
        }
        if (file.getSize() > properties.maxSizeBytes()) {
            throw new InvalidFileException("File size must not exceed " + properties.maxSizeBytes() / 1024 + " KB");
        }
        String contentType = file.getContentType();
        if (contentType == null
                || !properties.allowedTypes().contains(contentType)
                || !EXTENSIONS_BY_TYPE.containsKey(contentType)) {
            throw new InvalidFileException("Unsupported file type. Allowed types: " + properties.allowedTypes());
        }
        String originalName = file.getOriginalFilename();
        if (originalName != null && !hasAllowedExtension(originalName)) {
            throw new InvalidFileException("Unsupported file extension. Allowed: .jpg, .jpeg, .png, .webp");
        }
        return contentType;
    }

    private boolean hasAllowedExtension(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp");
    }
}
