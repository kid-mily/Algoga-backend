package com.kidmily.algoga_server.community.application.usecase;

import com.kidmily.algoga_server.community.domain.model.PostTagType;
import com.kidmily.algoga_server.community.presentation.api.response.AdminPostListResponse;
import com.kidmily.algoga_server.community.presentation.api.response.PostListResponse;
import com.kidmily.algoga_server.community.presentation.api.response.PostResponse;
import com.kidmily.algoga_server.community.presentation.api.response.TagResponse;

import java.util.List;

public interface PostQueryUseCase {
    PostResponse getPost(Long postId);

    // 태그 목록 조회
    List<PostTagType> getCategories();

    // 전체 게시글 목록 조회
    PostListResponse getPosts(Long lastPostId, List<PostTagType> categories, Long countryId);

    // 내가 작성한 글 목록 조회
    PostListResponse getMyPosts(Long userId, Long lastPostId, List<PostTagType> categories);

    // 관리자용 유저별 게시글 목록 조회
    AdminPostListResponse getMyPostsByPage(Long userId, Integer index, List<PostTagType> categories);

    // 관리자용 게시글 상세 조회 (조회수 증가 없음)
    PostResponse getPostForAdmin(Long postId);

    // 신규: 게시글 목록 필터바용 태그 (카테고리 + 인기 나라 5개)
    List<TagResponse> getPostFilterTags();
}