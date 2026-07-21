package com.kidmily.algoga_server.qna.application.service;

import com.kidmily.algoga_server.course.application.port.UserProfilePort;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.enrollment.domain.repository.EnrollmentRepository;
import com.kidmily.algoga_server.qna.exception.QnaErrorCode;
import com.kidmily.algoga_server.qna.exception.QnaException;
import com.kidmily.algoga_server.qna.application.command.AnswerCourseQnaCommand;
import com.kidmily.algoga_server.qna.application.command.CreateCourseQnaCommand;
import com.kidmily.algoga_server.qna.application.command.CreateCourseQnaCommentCommand;
import com.kidmily.algoga_server.qna.application.result.CourseQnaCommentResult;
import com.kidmily.algoga_server.qna.application.result.CourseQnaDetailResult;
import com.kidmily.algoga_server.qna.application.result.CourseQnaResult;
import com.kidmily.algoga_server.qna.application.usecase.CourseQnaUseCase;
import com.kidmily.algoga_server.qna.domain.model.CourseQna;
import com.kidmily.algoga_server.qna.domain.model.CourseQnaComment;
import com.kidmily.algoga_server.qna.domain.repository.CourseQnaCommentRepository;
import com.kidmily.algoga_server.qna.domain.repository.CourseQnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kidmily.algoga_server.notification.domain.event.QnaAnsweredEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseQnaService implements CourseQnaUseCase {

    private final CourseQnaRepository courseQnaRepository;
    private final CourseQnaCommentRepository courseQnaCommentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserProfilePort userProfilePort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public CourseQnaResult createQna(CreateCourseQnaCommand command) {
        validateAccessibleEnrollment(command.userId(), command.courseId());
        findCourseIncludingDeleted(command.courseId());

        CourseQna courseQna = CourseQna.create(
                command.courseId(),
                command.userId(),
                command.title(),
                command.question()
        );

        return toCourseQnaResult(courseQnaRepository.save(courseQna));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseQnaResult> getQnas(Long courseId) {
        findCourseIncludingDeleted(courseId);

        return courseQnaRepository.findByCourseId(courseId).stream()
                .map(this::toCourseQnaResult)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseQnaDetailResult getQnaDetail(Long courseId, Long qnaId) {
        findCourseIncludingDeleted(courseId);

        CourseQna qna = findQna(courseId, qnaId);
        List<CourseQnaComment> comments = courseQnaCommentRepository.findByQnaId(qnaId);

        return new CourseQnaDetailResult(
                qna.getId(),
                qna.getCourseId(),
                qna.getUserId(),
                profileValue(qna.getUserId(), UserProfilePort.UserProfile::username),
                profileValue(qna.getUserId(), UserProfilePort.UserProfile::name),
                profileValue(qna.getUserId(), UserProfilePort.UserProfile::email),
                profileValue(qna.getUserId(), UserProfilePort.UserProfile::nickname),
                qna.getManagerId(),
                qna.getTitle(),
                qna.getQuestion(),
                qna.getAnswer(),
                qna.getStatus(),
                qna.getCreatedAt(),
                qna.getAnsweredAt(),
                comments.stream()
                        .map(this::toCourseQnaCommentResult)
                        .toList()
        );
    }

    @Override
    public CourseQnaResult answerQna(AnswerCourseQnaCommand command) {
        findCourseIncludingDeleted(command.courseId());

        CourseQna qna = findQna(command.courseId(), command.qnaId());

        if ("ANSWERED".equals(qna.getStatus())) {
            throw new QnaException(QnaErrorCode.QNA_ALREADY_ANSWERED);
        }

        CourseQna answeredQna = qna.answer(command.managerId(), command.answer());
        CourseQnaResult result = toCourseQnaResult(courseQnaRepository.save(answeredQna));

        eventPublisher.publishEvent(new QnaAnsweredEvent(
                answeredQna.getUserId(),
                "관리자",
                command.qnaId(),
                command.answer()
        ));
        return result;
    }

    @Override
    public CourseQnaCommentResult createComment(CreateCourseQnaCommentCommand command) {
        findCourseIncludingDeleted(command.courseId());
        findQna(command.courseId(), command.qnaId());
        validateParentComment(command.qnaId(), command.parentCommentId());

        if ("USER".equals(command.writerType())) {
            validateAccessibleEnrollment(command.writerId(), command.courseId());
        }

        CourseQnaComment comment = "MANAGER".equals(command.writerType())
                ? CourseQnaComment.createManagerComment(
                command.qnaId(),
                command.parentCommentId(),
                command.writerId(),
                command.content()
        )
                : CourseQnaComment.createUserComment(
                command.qnaId(),
                command.parentCommentId(),
                command.writerId(),
                command.content()
        );

        return toCourseQnaCommentResult(courseQnaCommentRepository.save(comment));
    }

    private void validateParentComment(Long qnaId, Long parentCommentId) {
        if (parentCommentId == null) {
            return;
        }

        CourseQnaComment parentComment = courseQnaCommentRepository.findByIdAndQnaId(parentCommentId, qnaId)
                .orElseThrow(() -> new QnaException(QnaErrorCode.QNA_COMMENT_NOT_FOUND));

        if (parentComment.isDeleted() || parentComment.getParentCommentId() != null) {
            throw new QnaException(QnaErrorCode.QNA_COMMENT_NOT_FOUND);
        }
    }

    private CourseQna findQna(Long courseId, Long qnaId) {
        return courseQnaRepository.findByIdAndCourseId(qnaId, courseId)
                .orElseThrow(() -> new QnaException(QnaErrorCode.QNA_NOT_FOUND));
    }

    private CourseQnaResult toCourseQnaResult(CourseQna qna) {
        return CourseQnaResult.from(qna, findProfile(qna.getUserId()));
    }

    private CourseQnaCommentResult toCourseQnaCommentResult(CourseQnaComment comment) {
        UserProfilePort.UserProfile profile = "USER".equals(comment.getWriterType())
                ? findProfile(comment.getUserId())
                : null;

        return CourseQnaCommentResult.from(comment, profile);
    }

    private UserProfilePort.UserProfile findProfile(Long userId) {
        return userProfilePort.findProfile(userId)
                .orElse(null);
    }

    private String profileValue(
            Long userId,
            Function<UserProfilePort.UserProfile, String> mapper
    ) {
        UserProfilePort.UserProfile profile = findProfile(userId);
        return profile == null ? null : mapper.apply(profile);
    }

    private void validateAccessibleEnrollment(Long userId, Long courseId) {
        boolean accessible = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .map(enrollment -> enrollment.isAccessibleAt(LocalDateTime.now()))
                .orElse(false);

        if (!accessible) {
            throw new QnaException(QnaErrorCode.NOT_ENROLLED);
        }
    }

    private void findCourseIncludingDeleted(Long courseId) {
        courseRepository.findById(courseId)
                .orElseThrow(() -> new QnaException(QnaErrorCode.COURSE_NOT_FOUND));
    }
}
