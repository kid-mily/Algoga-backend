package com.kidmily.algoga_server.lms.presentation.api.admin;

import com.kidmily.algoga_server.admin.settings.annotation.CurrentManager;
import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.global.common.api.response.PageResponse;
import com.kidmily.algoga_server.global.exception.GlobalErrorCode;
import com.kidmily.algoga_server.lms.application.command.CreateCourseCommand;
import com.kidmily.algoga_server.lms.application.command.UpdateCourseCommand;
import com.kidmily.algoga_server.lms.application.usecase.AdminContentUseCase;
import com.kidmily.algoga_server.lms.domain.model.Course;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.presentation.request.admin.CreateCourseRequest;
import com.kidmily.algoga_server.lms.presentation.request.admin.UpdateCourseRequest;
import com.kidmily.algoga_server.lms.presentation.response.AdminCourseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Admin Course", description = "콘텐츠 매니저 강의 관리 API")
@RestController
@RequestMapping("/api/v1/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {

    private final AdminContentUseCase adminContentUseCase;

    @Operation(
            summary = "어드민 강의 생성",
            description = "콘텐츠 매니저가 강의 기본 정보와 썸네일, 선택 첨부파일을 등록합니다."
    )
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {"INVALID_REQUEST"})
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {"COUNTRY_NOT_FOUND", "FILE_UPLOAD_FAILED"})
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Long>> createCourse(
            @Parameter(description = "강의 생성 요청 JSON")
            @Valid @RequestPart(value = "request") CreateCourseRequest request,

            @Parameter(description = "강의 썸네일 이미지 파일", example = "osaka.png")
            @RequestPart(value = "thumbnail") MultipartFile thumbnailFile,

            @Parameter(description = "강의 첨부파일. 선택값입니다.", example = "osaka-guide.pdf")
            @RequestPart(value = "file", required = false) MultipartFile attachedFile,

            @CurrentManager Long managerId
    ) {
        CreateCourseCommand command = new CreateCourseCommand(
                request.countryId(),
                managerId,
                request.title(),
                request.description(),
                request.price(),
                request.level(),
                thumbnailFile,
                attachedFile
        );

        Long savedCourseId = adminContentUseCase.createCourse(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "COURSE_CREATED",
                        "강의 생성에 성공했습니다.",
                        savedCourseId
                ));
    }

    @Operation(
            summary = "어드민 강의 목록 조회",
            description = "삭제되지 않은 강의 목록을 최신순으로 조회합니다."
    )
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminCourseResponse>>> getCourses(
            @ParameterObject Pageable pageable
    ) {
        Page<AdminCourseResponse> response = adminContentUseCase.getCourses(pageable)
                .map(AdminCourseResponse::from);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "ADMIN_COURSES_FOUND",
                        "어드민 강의 목록 조회에 성공했습니다.",
                        PageResponse.from(response)
                )
        );
    }

    @Operation(
            summary = "어드민 강의 상세 조회",
            description = "강의 ID를 기준으로 삭제되지 않은 강의 상세 정보를 조회합니다."
    )
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {"COURSE_NOT_FOUND"})
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<AdminCourseResponse>> getCourse(
            @Parameter(description = "강의 ID", example = "1")
            @PathVariable Long courseId
    ) {
        Course course = adminContentUseCase.getCourse(courseId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "ADMIN_COURSE_FOUND",
                        "어드민 강의 상세 조회에 성공했습니다.",
                        AdminCourseResponse.from(course)
                )
        );
    }

    @Operation(
            summary = "어드민 강의 수정",
            description = "강의 제목, 설명, 가격, 난이도, 썸네일, 첨부파일을 수정합니다. 파일을 보내지 않으면 기존 파일 경로를 유지합니다."
    )
    @ApiErrorCodeExample(domain = GlobalErrorCode.class, value = {"INVALID_REQUEST"})
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {"COURSE_NOT_FOUND", "FILE_UPLOAD_FAILED"})
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PutMapping(value = "/{courseId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AdminCourseResponse>> updateCourse(
            @Parameter(description = "강의 ID", example = "1")
            @PathVariable Long courseId,

            @Parameter(description = "강의 수정 요청 JSON")
            @Valid @RequestPart(value = "request") UpdateCourseRequest request,

            @Parameter(description = "변경할 썸네일 이미지 파일. 선택값입니다.", example = "osaka-new.png")
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnailFile,

            @Parameter(description = "변경할 첨부파일. 선택값입니다.", example = "osaka-guide-new.pdf")
            @RequestPart(value = "file", required = false) MultipartFile attachedFile
    ) {
        UpdateCourseCommand command = new UpdateCourseCommand(
                request.title(),
                request.description(),
                request.price(),
                request.level(),
                thumbnailFile,
                attachedFile
        );

        Course updatedCourse = adminContentUseCase.updateCourse(courseId, command);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "COURSE_UPDATED",
                        "강의 수정에 성공했습니다.",
                        AdminCourseResponse.from(updatedCourse)
                )
        );
    }

    @Operation(
            summary = "어드민 강의 삭제",
            description = "강의를 실제 삭제하지 않고 Soft Delete 처리합니다."
    )
    @ApiErrorCodeExample(domain = LmsErrorCode.class, value = {"COURSE_NOT_FOUND"})
    @PreAuthorize("hasAnyAuthority('CONTENT_MANAGER', 'ROLE_CONTENT_MANAGER', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    @DeleteMapping("/{courseId}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(
            @Parameter(description = "강의 ID", example = "1")
            @PathVariable Long courseId
    ) {
        adminContentUseCase.deleteCourse(courseId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "COURSE_DELETED",
                        "강의 삭제에 성공했습니다."
                )
        );
    }
}