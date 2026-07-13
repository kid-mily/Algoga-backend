package com.kidmily.algoga_server.course.application.service;

// [리팩토링] 파일 스토리지 책임이 CourseFileManager로 이관되어 이 mock 타입은 미사용. 삭제 대신 이력 보존용으로 주석 처리함.
//import com.kidmily.algoga_server.global.port.out.FileStoragePort;
import com.kidmily.algoga_server.course.application.port.UserProfilePort;
import com.kidmily.algoga_server.course.application.result.CourseClassroomResult;
import com.kidmily.algoga_server.course.domain.model.Chapter;
import com.kidmily.algoga_server.course.domain.model.Course;
import com.kidmily.algoga_server.enrollment.domain.model.Enrollment;
import com.kidmily.algoga_server.enrollment.domain.model.EnrollmentStatus;
import com.kidmily.algoga_server.learningprogress.domain.model.LearningProgress;
import com.kidmily.algoga_server.course.domain.repository.ChapterRepository;
import com.kidmily.algoga_server.completion.domain.repository.CourseCompletionRepository;
import com.kidmily.algoga_server.qna.domain.repository.CourseQnaCommentRepository;
import com.kidmily.algoga_server.qna.domain.repository.CourseQnaRepository;
import com.kidmily.algoga_server.course.domain.repository.CourseRepository;
import com.kidmily.algoga_server.review.domain.repository.CourseReviewRepository;
import com.kidmily.algoga_server.enrollment.domain.repository.EnrollmentRepository;
import com.kidmily.algoga_server.learningprogress.domain.repository.LearningProgressRepository;
import com.kidmily.algoga_server.country.domain.repository.MapRepository;
import com.kidmily.algoga_server.quiz.domain.repository.QuizSubmissionRepository;
import com.kidmily.algoga_server.learning.exception.LearningErrorCode;
import com.kidmily.algoga_server.learning.exception.LearningException;
// [리팩토링] 파일 스토리지 설정 의존성이 CourseFileManager로 이관되어 이 mock 타입은 미사용. 삭제 대신 이력 보존용으로 주석 처리함.
//import com.kidmily.algoga_server.course.settings.CourseStorageSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    @Mock private UserProfilePort userProfilePort;
    // [리팩토링] 파일 스토리지 책임이 CourseFileManager로 이관되어 CourseService 생성자에서 제거됨. 아래 두 mock은 미사용이 되어 주석 처리함.
    //@Mock private FileStoragePort fileStoragePort;
    //@Mock private CourseStorageSettings storageSettings;
    @Mock private CourseFileManager courseFileManager;
    // [리팩토링] 진도 조회/병합 책임이 CourseProgressReader로 이관되어 CourseService 생성자에 추가됨.
    @Mock private CourseProgressReader courseProgressReader;

    @InjectMocks
    private CourseService courseService;

    @Test
    void locksNextChapterUntilPreviousChapterIsCompleted() {
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
    void opensQuizWhenAllRegisteredChaptersAreCompleted() {
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
    void rejectsClassroomAccessWhenEnrollmentExpired() {
        when(enrollmentRepository.findByUserIdAndCourseId(USER_ID, COURSE_ID))
                .thenReturn(Optional.of(enrollment(LocalDateTime.now().minusSeconds(1))));

        LearningException exception = assertThrows(
                LearningException.class,
                () -> courseService.getCourseClassroom(USER_ID, COURSE_ID)
        );

        assertSame(LearningErrorCode.NOT_ENROLLED, exception.getErrorCode());
    }

    @Test
    void deletedCourseRemainsAvailableToEnrolledStudent() {
        Chapter chapter = chapter(1L, 1, "https://cdn.test/chapter-1.mp4");
        mockClassroom(List.of(chapter), List.of());

        CourseClassroomResult result = courseService.getCourseClassroom(USER_ID, COURSE_ID);

        assertEquals(COURSE_ID, result.courseId());
        verify(courseRepository).findById(COURSE_ID);
    }

    @Test
    void deletingCourseDoesNotDeleteStoredFiles() {
        Course course = mock(Course.class);
        when(courseRepository.findByIdAndDeletedFalse(COURSE_ID)).thenReturn(Optional.of(course));
        when(courseRepository.softDelete(COURSE_ID)).thenReturn(true);

        courseService.deleteCourse(COURSE_ID);

        verify(courseRepository).softDelete(COURSE_ID);
        // [리팩토링] 파일 처리 책임이 CourseFileManager로 이관됨. 기존 fileStoragePort 검증을 courseFileManager 검증으로 대체함.
        //verifyNoInteractions(fileStoragePort);
        verifyNoInteractions(courseFileManager);
    }

    private void mockClassroom(List<Chapter> chapters, List<LearningProgress> progresses) {
        Course course = mock(Course.class);
        when(course.getId()).thenReturn(COURSE_ID);
        when(course.getTitle()).thenReturn("Travel course");
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByUserIdAndCourseId(USER_ID, COURSE_ID))
                .thenReturn(Optional.of(enrollment(LocalDateTime.now().plusMonths(6))));
        when(chapterRepository.findByCourseId(COURSE_ID)).thenReturn(chapters);
        // [리팩토링] 진도 조회/병합이 CourseProgressReader로 이관됨. 기존 learningProgressRepository 스텁을 reader 스텁으로 대체함.
        //when(learningProgressRepository.findByUserIdAndCourseId(USER_ID, COURSE_ID)).thenReturn(progresses);
        Map<Long, LearningProgress> progressByChapterId = progresses.stream()
                .collect(Collectors.toMap(LearningProgress::getChapterId, Function.identity()));
        when(courseProgressReader.loadProgressMapWithCache(eq(USER_ID), eq(COURSE_ID), anyList()))
                .thenReturn(progressByChapterId);
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
                chapterOrder + " chapter",
                null,
                videoUrl,
                600,
                chapterOrder,
                false
        );
    }
}
