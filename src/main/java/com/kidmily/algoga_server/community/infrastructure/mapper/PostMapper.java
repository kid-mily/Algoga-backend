package com.kidmily.algoga_server.community.infrastructure.mapper;
import com.kidmily.algoga_server.community.domain.model.Post;
import com.kidmily.algoga_server.community.infrastructure.persistence.entity.PostImage;
import com.kidmily.algoga_server.community.infrastructure.persistence.entity.PostJpaEntity;
import com.kidmily.algoga_server.community.infrastructure.persistence.entity.PostTag;
import com.kidmily.algoga_server.community.domain.model.PostTagType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PostMapper {

    // toJpaEntity는 필드 구조가 달라 자동 매핑 불가 → default로 직접 구현
    default PostJpaEntity toJpaEntity(Post post) {
        if (post == null) return null;

        PostJpaEntity jpaEntity = PostJpaEntity.builder()
                .authorId(post.getAuthorId())
                .title(post.getTitle())
                .content(post.getContent())
                .countryId(post.getCountryId())
                .lectureId(post.getLectureId())
                .build();

        // 카테고리 태그 추가
        if (post.getCategory() != null) {
            PostTag categoryTag = PostTag.builder()
                    .post(jpaEntity)
                    .tagType(post.getCategory())
                    .tagName(post.getCategory().name())
                    .build();
            jpaEntity.getPostTags().add(categoryTag);
        }

        // 자유 태그 추가
        if (post.getFreeTags() != null) {
            post.getFreeTags().forEach(tagName -> {
                PostTag freeTag = PostTag.builder()
                        .post(jpaEntity)
                        .tagType(PostTagType.FREE)
                        .tagName(tagName)
                        .build();
                jpaEntity.getPostTags().add(freeTag);
            });
        }

        // 이미지 추가
        if (post.getImageUrls() != null) {
            for (int i = 0; i < post.getImageUrls().size(); i++) {
                PostImage image = PostImage.builder()
                        .post(jpaEntity)
                        .imageUrl(post.getImageUrls().get(i))
                        .orderNum(i)
                        .build();
                jpaEntity.getPostImages().add(image);
            }
        }

        return jpaEntity;
    }

    // toDomain도 reconstitute 사용으로 default
    default Post toDomain(PostJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }

        PostTagType category = jpaEntity.getPostTags().stream()
                .filter(tag -> tag.getTagType() != PostTagType.FREE)
                .map(PostTag::getTagType)
                .findFirst()
                .orElse(null);

        List<String> freeTags = jpaEntity.getPostTags().stream()
                .filter(tag -> tag.getTagType() == PostTagType.FREE)
                .map(PostTag::getTagName)
                .toList();

        List<String> imageUrls = jpaEntity.getPostImages().stream()
                .map(PostImage::getImageUrl)
                .toList();

        return Post.reconstitute(
                jpaEntity.getPostId(),
                jpaEntity.getAuthorId(),
                category,
                jpaEntity.getTitle(),
                jpaEntity.getContent(),
                jpaEntity.getCountryId(),
                jpaEntity.getLectureId(),
                freeTags,
                imageUrls,
                jpaEntity.getCreatedAt(),
                jpaEntity.getViewCount(),
                jpaEntity.getIsDeleted()
        );
    }
}