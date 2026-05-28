package com.kidmily.algoga_server.community.infrastructure.persistence;

import com.kidmily.algoga_server.community.domain.model.Post;
import com.kidmily.algoga_server.community.domain.repository.PostRepository;
import com.kidmily.algoga_server.community.infrastructure.mapper.PostMapper;
import com.kidmily.algoga_server.community.infrastructure.persistence.entity.PostImage;
import com.kidmily.algoga_server.community.infrastructure.persistence.entity.PostJpaEntity;
import com.kidmily.algoga_server.community.infrastructure.persistence.entity.PostTag;
import com.kidmily.algoga_server.community.domain.model.PostTagType;
import com.kidmily.algoga_server.community.infrastructure.persistence.repository.SpringDataPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
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
        PostJpaEntity jpaEntity = springDataRepository.findById(post.getId()).orElseThrow();


        // 기존 태그/이미지 전부 삭제 (orphanRemoval이 처리)
        jpaEntity.getPostTags().clear();
        jpaEntity.getPostImages().clear();

        // 필드 업데이트
        jpaEntity.update(
                post.getCategory(),
                post.getTitle(),
                post.getContent(),
                post.getCountryId(),
                post.getLectureId(),
                post.getViewCount()
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

        // 🌟 새 이미지 추가
        if (post.getImageUrls() != null) {
            for (int i = 0; i < post.getImageUrls().size(); i++) {
                jpaEntity.getPostImages().add(PostImage.builder()
                        .post(jpaEntity)
                        .imageUrl(post.getImageUrls().get(i))
                        .orderNum(i)
                        .build());
            }
        }

        PostJpaEntity savedEntity = springDataRepository.save(jpaEntity);
        return postMapper.toDomain(savedEntity);
    }

    @Override
    public void delete(Post post) {
        PostJpaEntity jpaEntity = springDataRepository.findById(post.getId()).orElseThrow();

        jpaEntity.softDelete();  // PostJpaEntity에 이미 있음
        springDataRepository.save(jpaEntity);
    }

    // 게시글 전체 조회
    @Override
    public List<Post> findPostsByCursor(Long lastPostId, int size, List<PostTagType> categories) {
        Pageable pageable = PageRequest.of(0, size);

        // 빈 리스트면 null로 처리해서 전체 조회
        List<PostTagType> categoriesParam = (categories == null || categories.isEmpty()) ? null : categories;

        List<PostJpaEntity> entities = springDataRepository.findPostsByCursor(
                lastPostId, categoriesParam, pageable);

        return entities.stream()
                .map(postMapper::toDomain)
                .toList();
    }

    @Override
    public List<Post> findMyPostsByCursor(Long userId, Long lastPostId, int size, List<PostTagType> categories) {
        Pageable pageable = PageRequest.of(0, size);

        // 빈 리스트면 null로 처리해서 전체 조회가 가능하도록 정제
        List<PostTagType> categoriesParam = (categories == null || categories.isEmpty()) ? null : categories;

        // 통합 Query 메서드 다이렉트 호출
        List<PostJpaEntity> entities = springDataRepository.findMyPostsByCursor(
                userId, lastPostId, categoriesParam, pageable);

        return entities.stream()
                .map(postMapper::toDomain)
                .toList();
    }
}
