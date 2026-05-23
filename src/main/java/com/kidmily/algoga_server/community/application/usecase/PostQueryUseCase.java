package com.kidmily.algoga_server.community.application.usecase;

import com.kidmily.algoga_server.community.presentation.api.response.PostResponse;

public interface PostQueryUseCase {
    PostResponse getPost(Long postId);
}