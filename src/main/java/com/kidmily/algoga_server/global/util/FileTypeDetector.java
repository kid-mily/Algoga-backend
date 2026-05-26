package com.kidmily.algoga_server.global.util;

import com.kidmily.algoga_server.global.type.FileType;
import org.springframework.web.multipart.MultipartFile;

public class FileTypeDetector {

    // 인스턴스화 방지 (유틸리티 클래스 규칙)
    private FileTypeDetector() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * MultipartFile을 받아서 정확한 FileType Enum을 반환합니다.
     */
    public static FileType determineFileType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return FileType.UNKNOWN;
        }

        String mimeType = file.getContentType();

        // 1차 판별: MIME Type 기준 (가장 정확함)
        if (mimeType != null) {
            String lowerMime = mimeType.toLowerCase();
            if (lowerMime.startsWith("image/")) return FileType.IMAGE;
            if (lowerMime.startsWith("video/")) return FileType.VIDEO;
            if (lowerMime.startsWith("audio/")) return FileType.AUDIO;
            if (lowerMime.equals("application/pdf")) return FileType.PDF;
        }

        // 2차 판별: MIME Type이 누락되었을 경우 확장자 기준 (안전망)
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            return switch (extension) {
                case "jpg", "jpeg", "png", "gif", "webp", "svg" -> FileType.IMAGE;
                case "mp4", "avi", "mkv", "mov", "webm" -> FileType.VIDEO;
                case "mp3", "wav", "aac", "flac" -> FileType.AUDIO;
                case "pdf" -> FileType.PDF;
                default -> FileType.UNKNOWN;
            };
        }

        return FileType.UNKNOWN;
    }
}