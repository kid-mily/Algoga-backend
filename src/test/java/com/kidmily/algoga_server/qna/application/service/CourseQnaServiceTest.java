package com.kidmily.algoga_server.qna.application.service;

import com.kidmily.algoga_server.course.application.port.UserProfilePort;
import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.enrollment.domain.repository.EnrollmentRepository;
import com.kidmily.algoga_server.qna.application.command.CreateCourseQnaCommentCommand;
import com.kidmily.algoga_server.qna.domain.model.CourseQna;
import com.kidmily.algoga_server.qna.domain.model.CourseQnaComment;
import com.kidmily.algoga_server.qna.domain.repository.CourseQnaCommentRepository;
import com.kidmily.algoga_server.qna.domain.repository.CourseQnaRepository;
import com.kidmily.algoga_server.qna.exception.QnaErrorCode;
import com.kidmily.algoga_server.qna.exception.QnaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseQnaServiceTest {

    @Mock private CourseQnaRepository courseQnaRepository;
    @Mock private CourseQnaCommentRepository courseQnaCommentRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private UserProfilePort userProfilePort;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CourseQnaService courseQnaService;

    @Test
    void rejectsReplyToSoftDeletedParentComment() {
        Long courseId = 1L;
        Long qnaId = 10L;
        Long parentCommentId = 100L;

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mock(Course.class)));
        when(courseQnaRepository.findByIdAndCourseId(qnaId, courseId))
                .thenReturn(Optional.of(sampleQna(qnaId, courseId)));

        CourseQnaComment deletedParent = CourseQnaComment.withId(
                parentCommentId, qnaId, null, null, 999L, "MANAGER", "답변입니다", true, null
        );
        when(courseQnaCommentRepository.findByIdAndQnaId(parentCommentId, qnaId))
                .thenReturn(Optional.of(deletedParent));

        CreateCourseQnaCommentCommand command = new CreateCourseQnaCommentCommand(
                courseId, qnaId, parentCommentId, 999L, "MANAGER", "삭제된 댓글에 답글"
        );

        QnaException exception = assertThrows(QnaException.class, () -> courseQnaService.createComment(command));

        assertSame(QnaErrorCode.QNA_COMMENT_NOT_FOUND, exception.getErrorCode());
        verify(courseQnaCommentRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void allowsReplyToNonDeletedParentComment() {
        Long courseId = 1L;
        Long qnaId = 10L;
        Long parentCommentId = 100L;

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mock(Course.class)));
        when(courseQnaRepository.findByIdAndCourseId(qnaId, courseId))
                .thenReturn(Optional.of(sampleQna(qnaId, courseId)));

        CourseQnaComment activeParent = CourseQnaComment.withId(
                parentCommentId, qnaId, null, null, 999L, "MANAGER", "답변입니다", false, null
        );
        when(courseQnaCommentRepository.findByIdAndQnaId(parentCommentId, qnaId))
                .thenReturn(Optional.of(activeParent));
        when(courseQnaCommentRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateCourseQnaCommentCommand command = new CreateCourseQnaCommentCommand(
                courseId, qnaId, parentCommentId, 999L, "MANAGER", "정상 답글"
        );

        courseQnaService.createComment(command);

        verify(courseQnaCommentRepository).save(org.mockito.ArgumentMatchers.any());
    }

    private CourseQna sampleQna(Long qnaId, Long courseId) {
        return CourseQna.withId(
                qnaId, courseId, 1L, null, "제목", "질문 내용", null, "PENDING",
                LocalDateTime.now(), null
        );
    }
}
