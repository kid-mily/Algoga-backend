package com.kidmily.algoga_server.community.domain.repository;

import com.kidmily.algoga_server.community.domain.model.Post;
import com.kidmily.algoga_server.community.domain.model.PostTagType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Application, Domain 계층이 사용할 레포지토리 포트(Port)
public interface PostRepository {
    Post save(Post post);
    Optional<Post> findById(Long id);
    Post update(Post post);
    void delete(Post post);
    // 전체 리스트 조회
    List<Post> findPostsByCursor(Long lastPostId, int size, List<PostTagType> categories, Long countryId);
    // 내가 쓴 글 리스트 조회
    List<Post> findMyPostsByCursor(Long userId, Long lastPostId, int size, List<PostTagType> categories);

    // 관리자 유저 게시글 리스트 조회
    List<Post> findMyPostsByPage(Long userId, int page, int size, List<PostTagType> categories);
    long countMyPosts(Long userId, List<PostTagType> categories);

    // 신규: 인기 나라 태그 (게시글 수 상위 N개)
    List<CountryTagCount> findTopCountryTags(int limit);

    void increaseViewCount(Long postId, long count);

    List<Post> findExpiredDeletedPosts(LocalDateTime threshold);
}
