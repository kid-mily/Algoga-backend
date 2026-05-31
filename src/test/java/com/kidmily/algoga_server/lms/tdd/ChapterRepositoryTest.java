package com.kidmily.algoga_server.lms.tdd;

import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.ChapterJpaEntity;
import com.kidmily.algoga_server.lms.infrastructure.persistence.entity.CourseJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChapterRepositoryTest {

    // @DataJpaTest가 아래 2개 Bean으로 등록.
    @Autowired
    private SpringDataChapterRepository chapterRepository;

    @Autowired
    private SpringDataCourseRepository courseRepository;



    @Test
    void 챕터_저장_및_조회_테스트() {

        // given
        // 강의 엔티티 생성.
        CourseJpaEntity course = new CourseJpaEntity(
                1L,                 // country_id = 국가 ID
                2L,                         // manager_id = 관리자 ID
                "TDD 테스트 강의",            // title = 강의 제목
                "Repository 테스트용 강의",   // description = 강의 설명
                10000,                      // price = 강의 가격
                "thumbnail.png",            // thumbnail_url = 썸네일 주소
                null,                       // file_url
                "BEGINNER",                 // level = 강의 난이도
                "DRAFT"                     // status = 강의 상태
        );

        // 강의 먼저 생성함.
        CourseJpaEntity savedCourse = courseRepository.save(course);

        // 저장된 강의 ID 챕터의 lecture_id로 사용함.
        Long lectureId = savedCourse.getId();

        // 저장할 챕터 엔티티 생성.
        ChapterJpaEntity chapter = new ChapterJpaEntity(
                lectureId,                    // lecture_id = 강의 ID
                "TDD 테스트 챕터",             // title = 챕터 제목
                "https://test-video.com",     // video_url = 영상 주소
                600,                          // duration_seconds = 영상 길이
                99                            // order_num = 챕터 순서
        );

        // when
        // 챕터 DB에 저장.
        ChapterJpaEntity savedChapter = chapterRepository.save(chapter);

        // 저장된 ID로 챕터 다시 조회.
        ChapterJpaEntity foundChapter = chapterRepository.findById(savedChapter.getId()).orElse(null);

        // then
        // 조회 결과가 null인지 아닌지 검증.
        assertNotNull(foundChapter);

        // 저장한 제목 그대로 조회되는지 검증.
        assertEquals("TDD 테스트 챕터", foundChapter.getTitle());
    }

    @Test
    void 강의_ID로_삭제되지_않은_챕터_목록_조회_테스트() {

        // given
        CourseJpaEntity course = new CourseJpaEntity(
                1L,
                2L,
                "TDD 목록 조회 강의",
                "Repository 테스트용 강의",
                10000,
                "thumbnail.png",
                null,
                "BEGINNER",
                "DRAFT"
        );

        // given
        // 강의 먼저 저장.
        CourseJpaEntity savedCourse = courseRepository.save(course);

        // 저장된 강의 ID 챕터의 lecture_id로 사용.
        Long lectureId = savedCourse.getId();

        // 첫 번째 챕터 저장.
        chapterRepository.save(new ChapterJpaEntity(lectureId, "TDD 챕터 1", "https://test-1.com", 300, 101));

        // 두 번째 챕터 저장.
        chapterRepository.save(new ChapterJpaEntity(lectureId, "TDD 챕터 2", "https://test-2.com", 400, 102));

        // when
        // 강의 ID로 삭제되지 않은 챕터 목록을 순서대로 조회.
        List<ChapterJpaEntity> chapters = chapterRepository.findByCourseIdAndDeletedFalseOrderByOrderNumAsc(lectureId);

        // then
        // 조회된 챕터 목록 비어 있지 않은지 검증.
        assertFalse(chapters.isEmpty());

        // 조회된 챕터들이 모두 같은 강의 ID를 가지고 있는지 검증.
        chapters.forEach(chapter -> assertEquals(lectureId, chapter.getCourseId()));
    }
}
