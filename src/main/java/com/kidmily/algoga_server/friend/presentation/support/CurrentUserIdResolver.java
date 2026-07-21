package com.kidmily.algoga_server.friend.presentation.support;

import com.kidmily.algoga_server.friend.exception.FriendErrorCode;
import com.kidmily.algoga_server.friend.exception.FriendException;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;

// FriendController의 모든 메서드가 반복하던 "principal null 체크 + user.getUser().getId()"를 한 곳으로 모은다.
// 로그인 안 됨(principal == null)과 "대상 유저 없음"은 성격이 다른 에러라 별도 코드(LOGIN_REQUIRED)로 구분한다.
public final class CurrentUserIdResolver {

    private CurrentUserIdResolver() {
    }

    public static Long resolveLoginRequired(CustomUserDetails principal) {
        if (principal == null) {
            throw new FriendException(FriendErrorCode.LOGIN_REQUIRED);
        }
        return principal.getUser().getId();
    }
}
