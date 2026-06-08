package com.kidmily.algoga_server.lms.presentation.response;

import com.kidmily.algoga_server.lms.domain.model.CourseFile;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "강의 자료 파일 응답")
public record CourseFileResponse(

        @Schema(description = "파일 URL")
        String fileUrl,

        @Schema(description = "업로드 당시 원본 파일명", example = "osaka-guide.pdf")
        String originalFileName,

        @Schema(description = "파일 노출 순서", example = "1")
        int fileOrder
) {

    public static CourseFileResponse from(CourseFile courseFile) {
        String originalFileName = courseFile.getOriginalFileName();

        return new CourseFileResponse(
                courseFile.getFileUrl(),
                originalFileName == null || originalFileName.isBlank()
                        ? extractFileName(courseFile.getFileUrl())
                        : originalFileName,
                courseFile.getFileOrder()
        );
    }

    private static String extractFileName(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return null;
        }

        int queryIndex = fileUrl.indexOf('?');
        String path = queryIndex >= 0 ? fileUrl.substring(0, queryIndex) : fileUrl;
        int slashIndex = path.lastIndexOf('/');

        return slashIndex >= 0 ? path.substring(slashIndex + 1) : path;
    }
}
