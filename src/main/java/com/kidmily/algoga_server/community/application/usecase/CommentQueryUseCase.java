package com.kidmily.algoga_server.community.application.usecase;

import com.kidmily.algoga_server.community.presentation.api.response.AdminCommentListItemResponse;
import com.kidmily.algoga_server.community.presentation.api.response.AdminCommentListResponse;

import java.util.List;
import java.util.Map;

public interface CommentQueryUseCase {
    AdminCommentListResponse getMyCommentsByPage(Long userId, Integer index);
    AdminCommentListItemResponse getCommentForAdmin(Long commentId);
    Map<Long, Long> countMyCommentsForUsers(List<Long> userIds);
}