package com.kidmily.algoga_server.user.presentation.response;

import com.kidmily.algoga_server.user.domain.UserRepository.SignupPathStat;

public record SignupPathStatResponse(
        String path,  // 가입 경로 (예: "검색", "지인추천", "광고" 등)
        Long count    // 해당 경로로 가입한 사람 수
) {
    public static SignupPathStatResponse from(SignupPathStat stat) {
        // 만약 가입 경로가 비어있다면 "기타"로 묶어주는 센스!
        String pathName = (stat.getPath() != null && !stat.getPath().isBlank())
                ? stat.getPath()
                : "기타";

        return new SignupPathStatResponse(pathName, stat.getCount());
    }
}