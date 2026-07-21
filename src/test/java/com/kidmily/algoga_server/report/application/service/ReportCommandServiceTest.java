package com.kidmily.algoga_server.report.application.service;

import com.kidmily.algoga_server.report.application.command.CreateReportCommand;
import com.kidmily.algoga_server.report.application.command.UpdateReportStatusCommand;
import com.kidmily.algoga_server.report.application.port.ReportTargetInfo;
import com.kidmily.algoga_server.report.application.port.ReportTargetPort;
import com.kidmily.algoga_server.report.domain.model.ReasonType;
import com.kidmily.algoga_server.report.domain.model.Report;
import com.kidmily.algoga_server.report.domain.model.ReportStatus;
import com.kidmily.algoga_server.report.domain.model.TargetType;
import com.kidmily.algoga_server.report.domain.repository.ReportRepository;
import com.kidmily.algoga_server.report.exception.ReportErrorCode;
import com.kidmily.algoga_server.report.exception.ReportException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportCommandServiceTest {

    @InjectMocks
    private ReportCommandService reportCommandService;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ReportTargetPort reportTargetPort;

    private static final Long REPORTER_ID = 1L;
    private static final Long REPORTED_USER_ID = 2L;
    private static final Long TARGET_ID = 100L;
    private static final Long REPORT_ID = 500L;

    private CreateReportCommand createCommand() {
        return new CreateReportCommand(
                REPORTER_ID, TARGET_ID, TargetType.POST, ReasonType.ABUSE, "욕설이 포함되어 있습니다."
        );
    }

    private Report savedReport() {
        return Report.reconstitute(
                REPORT_ID, REPORTER_ID, REPORTED_USER_ID, TARGET_ID,
                TargetType.POST, ReasonType.ABUSE, "욕설이 포함되어 있습니다.",
                ReportStatus.RECEIVED, null
        );
    }

    // ===== 신고 생성 =====

    @Test
    @DisplayName("신고 대상이 존재하면 신고를 저장하고 reportId를 반환한다.")
    void createReport_success() {
        // given
        given(reportTargetPort.findTargetInfo(TargetType.POST, TARGET_ID))
                .willReturn(Optional.of(new ReportTargetInfo(REPORTED_USER_ID, "제목", "본문")));
        given(reportRepository.existsByUserAndTarget(REPORTER_ID, TargetType.POST, TARGET_ID))
                .willReturn(false);
        given(reportRepository.save(any(Report.class))).willReturn(savedReport());

        // when
        Long result = reportCommandService.handle(createCommand());

        // then
        assertThat(result).isEqualTo(REPORT_ID);
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    @DisplayName("신고 대상이 존재하지 않으면 REPORT_TARGET_NOT_FOUND 예외가 발생한다.")
    void createReport_targetNotFound_throws() {
        // given
        given(reportTargetPort.findTargetInfo(TargetType.POST, TARGET_ID))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reportCommandService.handle(createCommand()))
                .isInstanceOf(ReportException.class)
                .hasMessage(ReportErrorCode.REPORT_TARGET_NOT_FOUND.getMessage());

        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 신고한 대상이면 REPORT_DUPLICATED 예외가 발생한다.")
    void createReport_duplicated_throws() {
        // given
        given(reportTargetPort.findTargetInfo(TargetType.POST, TARGET_ID))
                .willReturn(Optional.of(new ReportTargetInfo(REPORTED_USER_ID, "제목", "본문")));
        given(reportRepository.existsByUserAndTarget(REPORTER_ID, TargetType.POST, TARGET_ID))
                .willReturn(true);   // 이미 신고함

        // when & then
        assertThatThrownBy(() -> reportCommandService.handle(createCommand()))
                .isInstanceOf(ReportException.class)
                .hasMessage(ReportErrorCode.REPORT_DUPLICATED.getMessage());

        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("본인을 신고하면 REPORT_SELF_NOT_ALLOWED 예외가 발생한다.")
    void createReport_self_throws() {
        // given — 신고 대상의 작성자가 신고자 본인
        given(reportTargetPort.findTargetInfo(TargetType.POST, TARGET_ID))
                .willReturn(Optional.of(new ReportTargetInfo(REPORTER_ID, "제목", "본문")));
        given(reportRepository.existsByUserAndTarget(REPORTER_ID, TargetType.POST, TARGET_ID))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> reportCommandService.handle(createCommand()))
                .isInstanceOf(ReportException.class)
                .hasMessage(ReportErrorCode.REPORT_SELF_NOT_ALLOWED.getMessage());

        verify(reportRepository, never()).save(any());
    }

    // ===== 상태 변경 =====

    @Test
    @DisplayName("신고 상태를 변경하면 저장된다.")
    void updateStatus_success() {
        // given
        UpdateReportStatusCommand command = new UpdateReportStatusCommand(REPORT_ID, ReportStatus.COMPLETED);
        given(reportRepository.findById(REPORT_ID)).willReturn(Optional.of(savedReport()));

        // when
        reportCommandService.updateStatus(command);

        // then
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    @DisplayName("존재하지 않는 신고의 상태를 변경하면 REPORT_NOT_FOUND 예외가 발생한다.")
    void updateStatus_notFound_throws() {
        // given
        UpdateReportStatusCommand command = new UpdateReportStatusCommand(REPORT_ID, ReportStatus.COMPLETED);
        given(reportRepository.findById(REPORT_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reportCommandService.updateStatus(command))
                .isInstanceOf(ReportException.class)
                .hasMessage(ReportErrorCode.REPORT_NOT_FOUND.getMessage());

        verify(reportRepository, never()).save(any());
    }
}