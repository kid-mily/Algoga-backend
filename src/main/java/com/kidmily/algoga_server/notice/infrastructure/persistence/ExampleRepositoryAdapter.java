package com.kidmily.algoga_server.notice.infrastructure.persistence;

import com.kidmily.algoga_server.example.domain.model.Example;
import com.kidmily.algoga_server.example.domain.repository.ExampleRepository;
import com.kidmily.algoga_server.example.infrastructure.mapper.ExampleMapper;
import com.kidmily.algoga_server.example.infrastructure.persistence.ExampleJpaEntity;
import com.kidmily.algoga_server.example.infrastructure.persistence.SpringDataExampleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ExampleRepositoryAdapter implements ExampleRepository {

    private final SpringDataExampleRepository springDataRepository;
    private final ExampleMapper exampleMapper;

    @Override
    public Example save(Example example) {
        // MapStruct를 통한 Domain -> JPA Entity 매핑
        com.kidmily.algoga_server.example.infrastructure.persistence.ExampleJpaEntity jpaEntity = exampleMapper.toJpaEntity(example);

        // DB 저장
        ExampleJpaEntity savedEntity = springDataRepository.save(jpaEntity);

        // MapStruct를 통한 JPA Entity -> Domain Entity 매핑
        return exampleMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Example> findById(Long id) {
        // DB 조회 후 MapStruct 메서드 참조(Method Reference)를 활용하여 Domain 반환
        return springDataRepository.findById(id)
                .map(exampleMapper::toDomain);
    }
}