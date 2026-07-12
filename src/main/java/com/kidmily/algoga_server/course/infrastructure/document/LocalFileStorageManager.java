package com.kidmily.algoga_server.course.infrastructure.document;

import com.kidmily.algoga_server.learning.exception.LearningErrorCode;
import com.kidmily.algoga_server.learning.exception.LearningException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class LocalFileStorageManager {

    private final Path uploadRoot;

    public LocalFileStorageManager(
            @Value("${app.file.upload-root:uploads}") String uploadRoot
    ) {
        this.uploadRoot = Path.of(uploadRoot).toAbsolutePath().normalize();
    }

    public String uploadFile(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            Path uploadDirectory = uploadRoot.resolve(directory).normalize();
            Files.createDirectories(uploadDirectory);

            String originalFilename = file.getOriginalFilename();
            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String savedFilename = UUID.randomUUID() + extension;
            Path savedPath = uploadDirectory.resolve(savedFilename).normalize();

            file.transferTo(savedPath.toFile());

            return uploadRoot.relativize(savedPath).toString().replace("\\", "/");
        } catch (Exception e) {
            throw new LearningException(LearningErrorCode.FILE_UPLOAD_FAILED);
        }
    }
}
