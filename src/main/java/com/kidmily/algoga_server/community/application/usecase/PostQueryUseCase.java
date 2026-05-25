package com.kidmily.algoga_server.community.application.usecase;

import com.kidmily.algoga_server.community.domain.model.PostTagType;
import com.kidmily.algoga_server.community.presentation.api.response.PostListResponse;
import com.kidmily.algoga_server.community.presentation.api.response.PostResponse;

import java.util.List;

public interface PostQueryUseCase {
    PostResponse getPost(Long postId);

    // 태그 목록 조회
    List<PostTagType> getCategories();

    // 전체 게시글 목록 조회
    PostListResponse getPosts(Long lastPostId, List<PostTagType> categories);

    // 내가 작성한 글 목록 조회
    PostListResponse getMyPosts(Long userId, Long lastPostId, List<PostTagType> categories);
}