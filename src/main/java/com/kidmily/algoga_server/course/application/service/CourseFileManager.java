package com.kidmily.algoga_server.course.application.service;

import com.kidmily.algoga_server.course.application.port.CourseFileStoragePort;
import com.kidmily.algoga_server.course.application.port.UploadFile;
import com.kidmily.algoga_server.course.domain.model.CourseFile;
import com.kidmily.algoga_server.course.settings.CourseStorageSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * 코스 파일/썸네일 스토리지 처리 책임을 CourseService에서 분리한 협력 객체.
 *
 * <p>업로드/삭제 동작과 반환값은 기존 CourseService 로직과 동일하다.
 */
@Component
@RequiredArgsConstructor
public class CourseFileManager {

    private final CourseFileStoragePort fileStoragePort;
    private final CourseStorageSettings storageSettings;

    public String uploadThumbnail(UploadFile thumbnailFile) {
        if (thumbnailFile == null || thumbnailFile.isEmpty()) {
            return null;
        }

        return fileStoragePort.uploadFile(
                thumbnailFile,
                storageSettings.getCourseThumbnailDirectory()
        );
    }

    public String replaceThumbnail(String currentThumbnailUrl, UploadFile thumbnailFile) {
        if (thumbnailFile == null || thumbnailFile.isEmpty()) {
            return currentThumbnailUrl;
        }

        if (currentThumbnailUrl != null && !currentThumbnailUrl.isBlank()) {
            fileStoragePort.deleteFile(currentThumbnailUrl);
        }

        return fileStoragePort.uploadFile(
                thumbnailFile,
                storageSettings.getCourseThumbnailDirectory()
        );
    }

    public List<CourseFile> uploadCourseFiles(List<UploadFile> attachedFiles) {
        if (!hasAttachedFiles(attachedFiles)) {
            return List.of();
        }

        List<UploadFile> validFiles = attachedFiles.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();

        return IntStream.range(0, validFiles.size())
                .mapToObj(index -> {
                    UploadFile file = validFiles.get(index);

                    String fileUrl = fileStoragePort.uploadFile(
                            file,
                            storageSettings.getCourseFileDirectory()
                    );

                    return CourseFile.create(fileUrl, file.originalFilename(), index + 1);
                })
                .toList();
    }

    public boolean hasAttachedFiles(List<UploadFile> attachedFiles) {
        return attachedFiles != null
                && attachedFiles.stream().anyMatch(file -> file != null && !file.isEmpty());
    }

    public void deleteCourseFiles(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return;
        }

        Set<String> uniqueFileUrls = new LinkedHashSet<>(fileUrls);

        for (String fileUrl : uniqueFileUrls) {
            if (fileUrl != null && !fileUrl.isBlank()) {
                fileStoragePort.deleteFile(fileUrl);
            }
        }
    }
}
