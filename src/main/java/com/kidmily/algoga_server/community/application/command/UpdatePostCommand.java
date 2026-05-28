package com.kidmily.algoga_server.community.application.command;

import com.kidmily.algoga_server.community.domain.model.PostTagType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record UpdatePostCommand(
        Long postId,
        Long requesterId,
        PostTagType category,
        String title,
        String content,
        Long countryId,
        Long lectureId,
        List<String> freeTags,
        List<MultipartFile> images
) {}