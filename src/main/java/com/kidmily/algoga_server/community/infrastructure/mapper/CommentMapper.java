package com.kidmily.algoga_server.community.infrastructure.mapper;

import com.kidmily.algoga_server.community.domain.model.Comment;
import com.kidmily.algoga_server.community.infrastructure.persistence.entity.CommentJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {

    // toJpaEntity → 신규 생성 시 commentId/createdAt은 DB가 채움
    default CommentJpaEntity toJpaEntity(Comment comment) {
        if (comment == null) return null;

        return CommentJpaEntity.builder()
                .postId(comment.getPostId())
                .userId(comment.getUserId())
                .parentId(comment.getParentId())
                .content(comment.getContent())
                .build();
    }

    // toDomain → reconstitute 사용
    default Comment toDomain(CommentJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;

        return Comment.reconstitute(
                jpaEntity.getCommentId(),
                jpaEntity.getPostId(),
                jpaEntity.getUserId(),
                jpaEntity.getParentId(),
                jpaEntity.getContent(),
                jpaEntity.getIsDeleted(),
                jpaEntity.getCreatedAt()
        );
    }
}