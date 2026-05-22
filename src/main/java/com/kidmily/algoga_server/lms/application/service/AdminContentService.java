package com.kidmily.algoga_server.lms.application.service;

import com.kidmily.algoga_server.lms.application.command.CreateCourseCommand;
import com.kidmily.algoga_server.lms.application.usecase.AdminContentUseCase;
import com.kidmily.algoga_server.lms.domain.model.Course;
import com.kidmily.algoga_server.lms.domain.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional // 에러가 나면 모든 DB 작업을 롤백(취소)해줍니다.
public class AdminContentService implements AdminContentUseCase {

    // Spring Data JPA가 아니라, 우리가 만든 순수 인터페이스(Port)를 바라봅니다.
    private final CourseRepository courseRepository;

    @Override
    public Long createCourse(CreateCourseCommand command) {

        // 1. 도메인 객체 생성 (비즈니스 규칙 검증은 Course.create 안에서 일어남)
        Course newCourse = Course.create(
                command.countryId(),
                command.managerId(),
                command.title(),
                command.description(),
                command.thumbnailUrl(),
                command.fileUrl()
        );

        // 2. 레포지토리를 통해 저장 (내부적으로 어댑터가 JpaEntity로 통역해서 DB에 넣음)
        Course savedCourse = courseRepository.save(newCourse);

        // 3. 생성된 강의의 ID 반환
        return savedCourse.getId();
    }
}