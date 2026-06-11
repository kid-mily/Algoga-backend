package com.kidmily.algoga_server.benefit.presentation.api;

import com.kidmily.algoga_server.global.annotation.swagger.ApiErrorCodeExample;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.benefit.application.command.RewardCourseCommand;
import com.kidmily.algoga_server.benefit.application.usecase.CourseRewardUseCase;
import com.kidmily.algoga_server.benefit.exception.BenefitErrorCode;
import com.kidmily.algoga_server.benefit.presentation.response.CourseRewardResponse;
<<<<<<< HEAD
import com.kidmily.algoga_server.benefit.presentation.support.CurrentUserIdResolver;
=======
import com.kidmily.algoga_server.user.exception.AuthErrorCode;
import com.kidmily.algoga_server.user.exception.AuthException;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
>>>>>>> 9e394e2220795389f2b87882ee1f5f7586ebffc6
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "수료/혜택", description = "강의 수료 보상 지급 API")
@RestController
@RequestMapping("/api/v1/courses/{courseId}/rewards")
@RequiredArgsConstructor
public class CourseRewardController {

    private final CourseRewardUseCase courseRewardUseCase;

    @Operation(
            summary = "쿠폰 및 마일리지 지급",
            description = """
                    강의 이수 완료 및 퀴즈 제출 완료 후 사용자에게 쿠폰과 마일리지를 지급합니다.
                    콘텐츠 매니저가 등록한 해당 강의의 활성 쿠폰 정책을 모두 사용자에게 발급합니다.
                    퀴즈 정답 수와 강의 가격을 기준으로 마일리지를 차등 지급합니다.
                    4~5개 정답은 강의 가격의 10%, 2~3개 정답은 7%, 0~1개 정답은 5%를 지급합니다.
                    """
    )
    @ApiErrorCodeExample(domain = BenefitErrorCode.class, value = {
            "COURSE_NOT_FOUND",
            "COURSE_COMPLETION_NOT_FOUND",
            "QUIZ_NOT_SUBMITTED",
            "COURSE_REWARD_ALREADY_GRANTED"
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<ApiResponse<CourseRewardResponse>> rewardCourse(
            @Parameter(description = "강의 ID", example = "3")
            @PathVariable Long courseId,

            @AuthenticationPrincipal Object userDetails
    ) {
<<<<<<< HEAD
        Long currentUserId = CurrentUserIdResolver.resolveRequired(userDetails);
=======
        if (userDetails == null) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        Long currentUserId = userDetails.getUser().getId();
>>>>>>> 9e394e2220795389f2b87882ee1f5f7586ebffc6

        RewardCourseCommand command = new RewardCourseCommand(
                currentUserId,
                courseId
        );

        var result = courseRewardUseCase.rewardCourseWithDetails(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "COURSE_REWARD_GRANTED",
                        "쿠폰 및 마일리지 지급에 성공했습니다.",
                        CourseRewardResponse.from(result)
                ));
    }
}
