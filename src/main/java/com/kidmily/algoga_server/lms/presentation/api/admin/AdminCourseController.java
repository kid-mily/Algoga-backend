package com.kidmily.algoga_server.lms.presentation.api.admin;

import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.lms.application.command.CreateCourseCommand;
import com.kidmily.algoga_server.lms.application.usecase.AdminContentUseCase;
import com.kidmily.algoga_server.lms.infrastructure.document.LocalFileStorageManager;
import com.kidmily.algoga_server.lms.presentation.request.admin.CreateCourseRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {

    private final AdminContentUseCase adminContentUseCase;
    private final LocalFileStorageManager fileStorageManager;

    // consumes 속성으로 파일 업로드(MULTIPART_FORM_DATA)를 지원한다고 명시
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Long>> createCourse(
            @RequestPart(value = "request") CreateCourseRequest request,
            @RequestPart(value = "thumbnail") MultipartFile thumbnailFile,
            @RequestPart(value = "file", required = false) MultipartFile attachedFile // 첨부파일은 선택사항
    ) {
        Long currentManagerId = 1L; // 임시 어드민 ID

        // 1. 넘어온 파일들을 인텔리제이 폴더에 물리적으로 저장하고 URL을 얻어옵니다.
        String thumbnailUrl = fileStorageManager.uploadFile(thumbnailFile, "thumbnails");
        String fileUrl = fileStorageManager.uploadFile(attachedFile, "documents");

        // 2. 알아낸 URL을 Command 상자에 합쳐서 서비스로 넘깁니다. (클린 아키텍처)
        CreateCourseCommand command = new CreateCourseCommand(
                request.countryId(),
                currentManagerId,
                request.title(),
                request.description(),
                thumbnailUrl,
                fileUrl
        );

        // 3. 서비스 호출
        Long savedCourseId = adminContentUseCase.createCourse(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "COURSE_CREATED",
                        "강의 생성에 성공했습니다.",
                        savedCourseId
                ));
    }
}