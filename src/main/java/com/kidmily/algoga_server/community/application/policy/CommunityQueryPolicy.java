package com.kidmily.algoga_server.community.application.policy;

import com.kidmily.algoga_server.community.application.port.CountryPort;
import com.kidmily.algoga_server.community.application.port.CoursePort;
import com.kidmily.algoga_server.community.application.port.UserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommunityQueryPolicy {

    private final UserPort userPort;
    private final CountryPort countryPort;
    private final CoursePort coursePort;

    public String resolveNickname(Long userId) {
        return userPort.getNickname(userId);
    }

    public String resolveProfileImageUrl(Long userId) {
        return userPort.getProfileImageUrl(userId);
    }

    public String resolveCountryName(Long countryId) {
        return countryPort.getCountryName(countryId);
    }
}