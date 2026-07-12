package com.kidmily.algoga_server.course.presentation.api.admin;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.exception.GlobalErrorCode;
import com.kidmily.algoga_server.course.application.command.CreateChapterCommand;
import com.kidmily.algoga_server.course.application.command.UpdateChapterCommand;
import com.kidmily.algoga_server.course.application.usecase.ChapterUseCase;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.course.presentation.request.admin.CreateChapterRequest;
import com.kidmily.algoga_server.course.presentation.request.admin.UpdateChapterRequest;
import com.kidmily.algoga_server.course.presentation.response.AdminChapterResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Admin Chapter", description = "콘텐츠 매니저 챕터 관리 API")
@RestController
@RequestMapping("/api/v1/admin/courses/{courseId}/chapters")
@RequiredArgsConstructor
public class AdminChapterController {

    private final ChapterUseCase chapterUseCase;

    @Operation(
            summary = "챕터 목록 조회",
            description = "특정 강의에 등록된 챕터 목록을 조회합니다."
    )
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {"COURSE_NOT_FOUND"})
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminChapterResponse>>> getChapters(
            @Parameter(description = "강의 ID", example = "1")
            @PathVariable Long courseId
    ) {
        List<AdminChapterResponse> response = chapterUseCase.getChapters(courseId)
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
            description = "콘텐츠 매니저가 특정 강의에 챕터를 등록합니다."
    )
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {"INVALID_REQUEST"})
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {
            "COURSE_NOT_FOUND",
            "FILE_UPLOAD_FAILED",
            "CHAPTER_VIDEO_REQUIRED",
            "INVALID_CHAPTER_ORDER"
    })
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AdminChapterResponse>> createChapter(
            @Parameter(description = "강의 ID", example = "1")
            @PathVariable Long courseId,

            @Parameter(description = "챕터 등록 요청 JSON")
            @Valid @RequestPart(value = "request") CreateChapterRequest request,

            @Parameter(
                    description = "챕터 영상 파일",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE, schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart(value = "video") MultipartFile videoFile
    ) {

        CreateChapterCommand command = new CreateChapterCommand(
                courseId,
                request.title(),
                request.description(),
                videoFile,
                request.durationSeconds(),
                request.chapterOrder()
        );

        var savedChapter = chapterUseCase.createChapter(command);

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
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {
            "COURSE_NOT_FOUND",
            "CHAPTER_NOT_FOUND",
            "FILE_UPLOAD_FAILED",
            "INVALID_CHAPTER_ORDER"
    })
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PutMapping(value = "/{chapterId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AdminChapterResponse>> updateChapter(
            @Parameter(description = "강의 ID", example = "1")
            @PathVariable Long courseId,

            @Parameter(description = "챕터 ID", example = "1")
            @PathVariable Long chapterId,

            @Parameter(description = "챕터 수정 요청 JSON")
            @Valid @RequestPart(value = "request") UpdateChapterRequest request,

            @Parameter(
                    description = "변경할 챕터 영상 파일. 선택값입니다.",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE, schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart(value = "video", required = false) MultipartFile videoFile
    ) {

        UpdateChapterCommand command = new UpdateChapterCommand(
                request.title(),
                request.description(),
                videoFile,
                request.durationSeconds(),
                request.chapterOrder()
        );

        var updatedChapter = chapterUseCase.updateChapter(courseId, chapterId, command);

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
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @DeleteMapping("/{chapterId}")
    public ResponseEntity<ApiResponse<Void>> deleteChapter(
            @Parameter(description = "강의 ID", example = "1")
            @PathVariable Long courseId,

            @Parameter(description = "챕터 ID", example = "1")
            @PathVariable Long chapterId
    ) {
        chapterUseCase.deleteChapter(courseId, chapterId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "CHAPTER_DELETED",
                        "챕터 삭제에 성공했습니다."
                )
        );
    }
}
