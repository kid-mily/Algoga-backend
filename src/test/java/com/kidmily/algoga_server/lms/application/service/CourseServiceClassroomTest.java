package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.global.port.out.FileStoragePort;
import com.kidmily.algoga_server.lms.application.port.PaymentPort;
import com.kidmily.algoga_server.lms.application.port.UserProfilePort;
import com.kidmily.algoga_server.lms.application.result.CourseClassroomResult;
import com.kidmily.algoga_server.lms.domain.model.Chapter;
import com.kidmily.algoga_server.lms.domain.model.Course;
import com.kidmily.algoga_server.lms.domain.model.Enrollment;
import com.kidmily.algoga_server.lms.domain.model.EnrollmentStatus;
import com.kidmily.algoga_server.lms.domain.model.LearningProgress;
import com.kidmily.algoga_server.lms.domain.repository.ChapterRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseQnaCommentRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseQnaRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import com.kidmily.algoga_server.lms.domain.repository.CourseReviewRepository;
import com.kidmily.algoga_server.lms.domain.repository.EnrollmentRepository;
import com.kidmily.algoga_server.lms.domain.repository.LearningProgressRepository;
import com.kidmily.algoga_server.lms.domain.repository.MapRepository;
import com.kidmily.algoga_server.lms.domain.repository.QuizSubmissionRepository;
import com.kidmily.algoga_server.lms.exception.LmsErrorCode;
import com.kidmily.algoga_server.lms.exception.LmsException;
import com.kidmily.algoga_server.lms.settings.LmsStorageSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceClassroomTest {

    private static final Long USER_ID = 1L;
    private static final Long COURSE_ID = 76L;

    @Mock private CourseRepository courseRepository;
    @Mock private ChapterRepository chapterRepository;
    @Mock private LearningProgressRepository learningProgressRepository;
    @Mock private CourseCompletionRepository courseCompletionRepository;
    @Mock private QuizSubmissionRepository quizSubmissionRepository;
    @Mock private CourseReviewRepository courseReviewRepository;
    @Mock private CourseQnaRepository courseQnaRepository;
    @Mock private CourseQnaCommentRepository courseQnaCommentRepository;
    @Mock private MapRepository mapRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private PaymentPort paymentPort;
    @Mock private UserProfilePort userProfilePort;
    @Mock private FileStoragePort fileStoragePort;
    @Mock private LmsStorageSettings storageSettings;

    @InjectMocks
    private CourseService courseService;

    @Test
    void 이전_챕터를_완료하지_않으면_다음_챕터가_잠긴다() {
        Chapter first = chapter(1L, 1, "https://cdn.test/chapter-1.mp4");
        Chapter second = chapter(2L, 2, "https://cdn.test/chapter-2.mp4");
        mockClassroom(List.of(first, second), List.of());

        CourseClassroomResult result = courseService.getCourseClassroom(USER_ID, COURSE_ID);

        assertFalse(result.quizAvailable());
        assertFalse(result.chapters().get(0).locked());
        assertEquals(first.getVideoUrl(), result.chapters().get(0).videoUrl());
        assertTrue(result.chapters().get(1).locked());
        assertNull(result.chapters().get(1).videoUrl());
    }

    @Test
    void 등록된_모든_챕터를_완료하면_퀴즈가_열린다() {
        Chapter first = chapter(1L, 1, "https://cdn.test/chapter-1.mp4");
        Chapter second = chapter(2L, 2, "https://cdn.test/chapter-2.mp4");
        List<LearningProgress> progresses = List.of(
                LearningProgress.withId(1L, USER_ID, COURSE_ID, first.getId(), 600, 100, true),
                LearningProgress.withId(2L, USER_ID, COURSE_ID, second.getId(), 600, 100, true)
        );
        mockClassroom(List.of(first, second), progresses);

        CourseClassroomResult result = courseService.getCourseClassroom(USER_ID, COURSE_ID);

        assertTrue(result.quizAvailable());
        assertTrue(result.chapters().stream().allMatch(chapter -> chapter.completed() && !chapter.locked()));
    }

    @Test
    void 수강_기간이_만료되면_강의실에_접근할_수_없다() {
        Course course = mock(Course.class);
        when(courseRepository.findByIdAndDeletedFalse(COURSE_ID)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByUserIdAndCourseId(USER_ID, COURSE_ID))
                .thenReturn(Optional.of(enrollment(LocalDateTime.now().minusSeconds(1))));

        LmsException exception = assertThrows(
                LmsException.class,
                () -> courseService.getCourseClassroom(USER_ID, COURSE_ID)
        );

        assertSame(LmsErrorCode.NOT_ENROLLED, exception.getErrorCode());
    }

    private void mockClassroom(List<Chapter> chapters, List<LearningProgress> progresses) {
        Course course = mock(Course.class);
        when(course.getId()).thenReturn(COURSE_ID);
        when(course.getTitle()).thenReturn("여행 준비 강의");
        when(courseRepository.findByIdAndDeletedFalse(COURSE_ID)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByUserIdAndCourseId(USER_ID, COURSE_ID))
                .thenReturn(Optional.of(enrollment(LocalDateTime.now().plusMonths(6))));
        when(chapterRepository.findByCourseId(COURSE_ID)).thenReturn(chapters);
        when(learningProgressRepository.findByUserIdAndCourseId(USER_ID, COURSE_ID)).thenReturn(progresses);
    }

    private Enrollment enrollment(LocalDateTime accessExpiresAt) {
        return Enrollment.withId(
                1L,
                USER_ID,
                COURSE_ID,
                EnrollmentStatus.ENROLLED,
                LocalDateTime.now().minusDays(1),
                null,
                accessExpiresAt
        );
    }

    private Chapter chapter(Long chapterId, int chapterOrder, String videoUrl) {
        return Chapter.withId(
                chapterId,
                COURSE_ID,
                chapterOrder + "강",
                videoUrl,
                600,
                chapterOrder,
                false
        );
    }
}
