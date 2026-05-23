package com.kidmily.algoga_server.community.infrastructure.persistence;

import com.kidmily.algoga_server.community.domain.model.Post;
import com.kidmily.algoga_server.community.domain.repository.PostRepository;
import com.kidmily.algoga_server.community.exception.PostErrorCode;
import com.kidmily.algoga_server.community.infrastructure.mapper.PostMapper;
import com.kidmily.algoga_server.community.infrastructure.persistence.entity.PostJpaEntity;
import com.kidmily.algoga_server.community.infrastructure.persistence.entity.PostTag;
import com.kidmily.algoga_server.community.infrastructure.persistence.entity.PostTagType;
import com.kidmily.algoga_server.community.infrastructure.persistence.repository.SpringDataPostRepository;
import com.kidmily.algoga_server.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostRepositoryAdapter implements PostRepository {

    private final SpringDataPostRepository springDataRepository;
    private final PostMapper postMapper;

    @Override
    public Post save(Post post) {
        // MapStruct를 통한 Domain -> JPA Entity 매핑
        PostJpaEntity jpaEntity = postMapper.toJpaEntity(post);

        // DB 저장
        PostJpaEntity savedEntity = springDataRepository.save(jpaEntity);

        // MapStruct를 통한 JPA Entity -> Domain Entity 매핑
        return postMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Post> findById(Long id) {
        return springDataRepository.findById(id)
                .map(postMapper::toDomain);
    }

    @Override
    public Post update(Post post) {
        // 기존 엔티티 조회
        PostJpaEntity jpaEntity = springDataRepository.findById(post.getId())
                .orElseThrow(() -> new BusinessException(PostErrorCode.POST_NOT_FOUND));

        // 기존 태그/이미지 전부 삭제 (orphanRemoval이 처리)
        jpaEntity.getPostTags().clear();
        jpaEntity.getPostImages().clear();

        // 필드 업데이트
        jpaEntity.update(
                post.getCategory(),
                post.getTitle(),
                post.getContent(),
                post.getCountryId(),
                post.getLectureId()
        );

        // 새 태그 추가
        if (post.getCategory() != null) {
            jpaEntity.getPostTags().add(PostTag.builder()
                    .post(jpaEntity)
                    .tagType(post.getCategory())
                    .tagName(post.getCategory().name())
                    .build());
        }
        if (post.getFreeTags() != null) {
            post.getFreeTags().forEach(tagName ->
                    jpaEntity.getPostTags().add(PostTag.builder()
                            .post(jpaEntity)
                            .tagType(PostTagType.FREE)
                            .tagName(tagName)
                            .build()));
        }

        PostJpaEntity savedEntity = springDataRepository.save(jpaEntity);
        return postMapper.toDomain(savedEntity);
    }

    @Override
    public void delete(Post post) {
        PostJpaEntity jpaEntity = springDataRepository.findById(post.getId())
                .orElseThrow(() -> new BusinessException(PostErrorCode.POST_NOT_FOUND));
        jpaEntity.softDelete();  // PostJpaEntity에 이미 있음
        springDataRepository.save(jpaEntity);
    }
}
