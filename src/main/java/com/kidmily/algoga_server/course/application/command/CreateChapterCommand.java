package com.kidmily.algoga_server.course.application.command;

// [리팩토링] 웹 타입(MultipartFile)을 애플리케이션 계층에서 제거하고 내부 타입(UploadFile) 사용. 기존 import는 이력 보존용으로 주석 처리함.
//import org.springframework.web.multipart.MultipartFile;
import com.kidmily.algoga_server.course.application.port.UploadFile;

public record CreateChapterCommand(
        Long courseId,
        String title,
        String description,
        UploadFile videoFile, // [리팩토링] MultipartFile -> UploadFile
        int durationSeconds,
        int chapterOrder
) {
}