package com.kidmily.algoga_server.community.application.command;

import com.kidmily.algoga_server.community.infrastructure.persistence.entity.PostTagType;

import java.util.List;

public record UpdatePostCommand(
        Long postId,
        Long requesterId,
        PostTagType category,
        String title,
        String content,
        Long countryId,
        Long lectureId,
        List<String> freeTags
) {}