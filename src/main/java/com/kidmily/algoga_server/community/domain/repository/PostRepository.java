package com.kidmily.algoga_server.community.domain.repository;

import com.kidmily.algoga_server.community.domain.model.Post;

import java.util.Optional;

// Application, Domain 계층이 사용할 레포지토리 포트(Port)
public interface PostRepository {
    Post save(Post post);
    Optional<Post> findById(Long id);
    Post update(Post post);
    void delete(Post post);
}
