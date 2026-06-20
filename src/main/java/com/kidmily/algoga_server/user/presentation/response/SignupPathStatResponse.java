package com.kidmily.algoga_server.user.presentation.response;

import com.kidmily.algoga_server.user.domain.UserRepository.SignupPathStat;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자용 가입 경로별 유입 통계 응답")
public record SignupPathStatResponse(

        @Schema(description = "가입 경로 (빈 값일 경우 '기타'로 처리됨)", example = "검색")
        String path,

        @Schema(description = "해당 경로를 통해 가입한 유저 수", example = "125")
        Long count

) {
    /**
     * 🌟 DB 통계 인터페이스 결과를 프론트엔드 응답용 DTO로 변환
     */
    public static SignupPathStatResponse from(SignupPathStat stat) {
        // 만약 가입 경로가 비어있다면 "기타"로 묶어주는 센스!
        String pathName = (stat.getPath() != null && !stat.getPath().isBlank())
                ? stat.getPath()
                : "기타";

        return new SignupPathStatResponse(pathName, stat.getCount());
    }
}