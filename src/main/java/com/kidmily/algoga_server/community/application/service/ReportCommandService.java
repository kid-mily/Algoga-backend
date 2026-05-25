package com.kidmily.algoga_server.community.application.service;

import com.kidmily.algoga_server.community.application.command.CreateReportCommand;
import com.kidmily.algoga_server.community.application.usecase.ReportCommandUseCase;
import com.kidmily.algoga_server.community.domain.model.Report;
import com.kidmily.algoga_server.community.domain.repository.PostRepository;
import com.kidmily.algoga_server.community.domain.repository.CommentRepository;
import com.kidmily.algoga_server.community.domain.repository.ReportRepository;
import com.kidmily.algoga_server.community.domain.model.TargetType;
import com.kidmily.algoga_server.community.exception.CommentException;
import com.kidmily.algoga_server.community.exception.PostErrorCode;
import com.kidmily.algoga_server.community.exception.PostException;
import com.kidmily.algoga_server.community.exception.ReportException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReportCommandService implements ReportCommandUseCase {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Override
    public Long handle(CreateReportCommand command) {
        log.info("[ReportCommandService] 신고 요청 수신 - userId: {}, targetType: {}, targetId: {}",
                command.userId(), command.targetType(), command.targetId());

        // 대상 존재 여부 검증
        if (command.targetType() == TargetType.POST) {
            postRepository.findById(command.targetId())
                    .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
        } else if (command.targetType() == TargetType.COMMENT) {
            commentRepository.findById(command.targetId())
                    .orElseThrow(() -> new CommentException(PostErrorCode.COMMENT_NOT_FOUND));
        }

        // 중복 신고 검증
        if (reportRepository.existsByUserAndTarget(command.userId(), command.targetType(), command.targetId())) {
            throw new ReportException(PostErrorCode.REPORT_DUPLICATED);
        }

        Report report = Report.create(
                command.userId(),
                command.targetId(),
                command.targetType(),
                command.reasonType(),
                command.detail()
        );

        Report savedReport = reportRepository.save(report);

        log.info("[ReportCommandService] 신고 완료 - reportId: {}", savedReport.getReportId());

        return savedReport.getReportId();
    }
}