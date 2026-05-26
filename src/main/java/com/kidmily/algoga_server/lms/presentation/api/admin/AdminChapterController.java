package com.kidmily.algoga_server.lms.presentation.api.admin;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.exception.GlobalErrorCode;
import com.kidmily.algoga_server.lms.application.command.CreateChapterCommand;
import com.kidmily.algoga_server.lms.application.command.UpdateChapterCommand;
import com.kidmily.algoga_server.lms.application.usecase.AdminChapterUseCase;
import com.kidmily.algoga_server.lms.domain.model.Chapter;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.infrastructure.document.LocalFileStorageManager;
import com.kidmily.algoga_server.lms.presentation.request.admin.CreateChapterRequest;
import com.kidmily.algoga_server.lms.presentation.request.admin.UpdateChapterRequest;
import com.kidmily.algoga_server.lms.presentation.response.AdminChapterResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "챕터 관리", description = "콘텐츠 매니저 챕터 관리 API")
@RestController
@RequestMapping("/api/v1/courses/{courseId}/chapters")
@RequiredArgsConstructor
public class AdminChapterController {

    private final AdminChapterUseCase adminChapterUseCase;
    private final LocalFileStorageManager fileStorageManager;

    @Operation(
            summary = "챕터 목록 조회",
            description = "특정 강의에 등록된 챕터 목록을 노출 순서대로 조회합니다."
    )
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {"COURSE_NOT_FOUND"})
    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminChapterResponse>>> getChapters(
            @Parameter(description = "강의 ID", example = "1")
            @PathVariable Long courseId
    ) {
        List<AdminChapterResponse> response = adminChapterUseCase.getChapters(courseId)
                .stream()
                .map(AdminChapterResponse::from)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "ADMIN_CHAPTERS_FOUND",
                        "챕터 목록 조회에 성공했습니다.",
                        response
                )
        );
    }

    @Operation(
            summary = "챕터 등록",
            description = "특정 강의에 챕터 영상과 기본 정보를 등록합니다. 영상 파일은 필수입니다."
    )
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {"INVALID_REQUEST"})
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {"COURSE_NOT_FOUND", "FILE_UPLOAD_FAILED", "CHAPTER_VIDEO_REQUIRED", "INVALID_CHAPTER_ORDER"})
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AdminChapterResponse>> createChapter(
            @Parameter(description = "강의 ID", example = "1")
            @PathVariable Long courseId,

            @Parameter(description = "챕터 등록 요청 JSON")
            @Valid @RequestPart(value = "request") CreateChapterRequest request,

            @Parameter(description = "챕터 영상 파일", example = "chapter-video.mp4")
            @RequestPart(value = "video") MultipartFile videoFile
    ) {
        String videoUrl = fileStorageManager.uploadFile(videoFile, "videos");

        CreateChapterCommand command = new CreateChapterCommand(
                courseId,
                request.title(),
                videoUrl,
                request.durationSeconds(),
                request.chapterOrder()
        );

        Chapter savedChapter = adminChapterUseCase.createChapter(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "CHAPTER_CREATED",
                        "챕터 등록에 성공했습니다.",
                        AdminChapterResponse.from(savedChapter)
                ));
    }

    @Operation(
            summary = "챕터 수정",
            description = "특정 강의의 챕터 제목, 영상, 재생 시간, 노출 순서를 수정합니다. 영상 파일을 보내지 않으면 기존 영상 경로를 유지합니다."
    )
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {"INVALID_REQUEST"})
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {"COURSE_NOT_FOUND", "CHAPTER_NOT_FOUND", "FILE_UPLOAD_FAILED", "INVALID_CHAPTER_ORDER"})
    @PutMapping(value = "/{chapterId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AdminChapterResponse>> updateChapter(
            @Parameter(description = "강의 ID", example = "1")
            @PathVariable Long courseId,

            @Parameter(description = "챕터 ID", example = "1")
            @PathVariable Long chapterId,

            @Parameter(description = "챕터 수정 요청 JSON")
            @Valid @RequestPart(value = "request") UpdateChapterRequest request,

            @Parameter(description = "변경할 챕터 영상 파일. 선택값입니다.", example = "chapter-video-new.mp4")
            @RequestPart(value = "video", required = false) MultipartFile videoFile
    ) {
        String videoUrl = fileStorageManager.uploadFile(videoFile, "videos");

        UpdateChapterCommand command = new UpdateChapterCommand(
                request.title(),
                videoUrl,
                request.durationSeconds(),
                request.chapterOrder()
        );

        Chapter updatedChapter = adminChapterUseCase.updateChapter(
                courseId,
                chapterId,
                command
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "CHAPTER_UPDATED",
                        "챕터 수정에 성공했습니다.",
                        AdminChapterResponse.from(updatedChapter)
                )
        );
    }

    @Operation(
            summary = "챕터 삭제",
            description = "특정 강의의 챕터를 실제 삭제하지 않고 Soft Delete 처리합니다."
    )
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {"COURSE_NOT_FOUND", "CHAPTER_NOT_FOUND"})
    @DeleteMapping("/{chapterId}")
    public ResponseEntity<ApiResponse<Void>> deleteChapter(
            @Parameter(description = "강의 ID", example = "1")
            @PathVariable Long courseId,

            @Parameter(description = "챕터 ID", example = "1")
            @PathVariable Long chapterId
    ) {
        adminChapterUseCase.deleteChapter(courseId, chapterId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "CHAPTER_DELETED",
                        "챕터 삭제에 성공했습니다."
                )
        );
    }
}