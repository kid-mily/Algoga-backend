//package com.kidmily.algoga_server.benefit.presentation.api;
//
//import com.kidmily.algoga_server.benefit.application.command.RewardCourseCommand;
//import com.kidmily.algoga_server.benefit.application.usecase.CourseRewardUseCase;
//import com.kidmily.algoga_server.benefit.exception.BenefitErrorCode;
//import com.kidmily.algoga_server.benefit.presentation.response.CourseRewardResponse;
//import com.kidmily.algoga_server.benefit.presentation.support.CurrentUserIdResolver;
//import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
//import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@Tag(name = "Course Reward", description = "강의 수료 보상 지급 API")
//@RestController
//@RequestMapping("/api/v1/courses/{courseId}/rewards")
//@RequiredArgsConstructor
//public class CourseRewardController {
//
//    private final CourseRewardUseCase courseRewardUseCase;
//
//    @Operation(
//            summary = "쿠폰 및 마일리지 지급",
//            description = "강의 수료와 퀴즈 제출이 완료된 사용자에게 활성 쿠폰과 마일리지를 지급합니다."
//    )
//    @ApiErrorCodeExample(domain = BenefitErrorCode.class, value = {
//            "COURSE_NOT_FOUND",
//            "COURSE_COMPLETION_NOT_FOUND",
//            "QUIZ_NOT_SUBMITTED",
//            "COURSE_REWARD_ALREADY_GRANTED",
//            "COURSE_REWARD_PERIOD_EXPIRED"
//    })
//    @PreAuthorize("isAuthenticated()")
//    @PostMapping
//    public ResponseEntity<ApiResponse<CourseRewardResponse>> rewardCourse(
//            @Parameter(description = "강의 ID", example = "3")
//            @PathVariable Long courseId,
//
//            @AuthenticationPrincipal Object userDetails
//    ) {
//        Long currentUserId = CurrentUserIdResolver.resolveRequired(userDetails);
//        RewardCourseCommand command = new RewardCourseCommand(currentUserId, courseId);
//        var result = courseRewardUseCase.rewardCourseWithDetails(command);
//
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(ApiResponse.created(
//                        "COURSE_REWARD_GRANTED",
//                        "쿠폰 및 마일리지 지급에 성공했습니다.",
//                        CourseRewardResponse.from(result)
//                ));
//    }
//}
