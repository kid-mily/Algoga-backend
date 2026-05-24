package com.kidmily.algoga_server.community.domain.repository;

import com.kidmily.algoga_server.community.domain.model.Post;
import com.kidmily.algoga_server.community.infrastructure.persistence.entity.PostTagType;

import java.util.List;
import java.util.Optional;

// Application, Domain 계층이 사용할 레포지토리 포트(Port)
public interface PostRepository {
    Post save(Post post);
    Optional<Post> findById(Long id);
    Post update(Post post);
    void delete(Post post);
    // 전체 리스트 조회
    List<Post> findPostsByCursor(Long lastPostId, int size, List<PostTagType> categories);
    // 내가 쓴 글 리스트 조회
    List<Post> findMyPostsByCursor(Long userId, Long lastPostId, int size, List<PostTagType> categories);
}
