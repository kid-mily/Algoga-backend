package com.kidmily.algoga_server.report.infrastructure.adapter;

import com.kidmily.algoga_server.community.domain.repository.CommentRepository;
import com.kidmily.algoga_server.community.domain.repository.PostRepository;
import com.kidmily.algoga_server.report.application.port.ReportTargetInfo;
import com.kidmily.algoga_server.report.application.port.ReportTargetPort;
import com.kidmily.algoga_server.report.domain.model.TargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CommunityReportTargetAdapter implements ReportTargetPort {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Override
    public Optional<ReportTargetInfo> findTargetInfo(TargetType targetType, Long targetId) {
        if (targetType == TargetType.POST) {
            return postRepository.findById(targetId)
                    .map(post -> new ReportTargetInfo(post.getAuthorId()));
        } else {
            return commentRepository.findById(targetId)
                    .map(comment -> new ReportTargetInfo(comment.getUserId()));
        }
    }
}