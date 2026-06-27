package com.kidmily.algoga_server.community.application.service;

import com.kidmily.algoga_server.community.settings.cache.CommunityCacheType;
import com.kidmily.algoga_server.community.presentation.api.response.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostCacheService {

    private final PostQueryService postQueryService;

    // 조회수를 제외한 게시글 상세를 캐싱 (viewCount 자리는 0으로 채워둠)
    @Cacheable(cacheNames = CommunityCacheType.Const.POST_DETAIL, key = "#postId")
    public PostResponse getCachedPostContent(Long postId) {
        return postQueryService.buildPostResponseWithoutViewCount(postId);
    }
}