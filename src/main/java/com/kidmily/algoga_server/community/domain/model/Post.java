package com.kidmily.algoga_server.community.domain.model;

import com.kidmily.algoga_server.community.exception.PostErrorCode;
import com.kidmily.algoga_server.community.exception.PostException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    private static final int MAX_IMAGE_COUNT = 10;
    private static final int MAX_FREE_TAG_COUNT = 10;

    private Long id;
    private Long authorId;
    private PostTagType category;
    private String title;
    private String content;
    private Long countryId;
    private Long lectureId;
    private List<String> freeTags;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private Boolean isDeleted;
    private Integer viewCount;

    // 1. 생성 시점의 생성자
    private Post(Long authorId, PostTagType category, String title, String content,
                 Long countryId, Long lectureId, List<String> freeTags, List<String> imageUrls) {
        validateCategory(category);
        validateTitle(title);
        validateContent(content);
        validateFreeTags(freeTags);
        validateImages(imageUrls);

        this.authorId = authorId;
        this.category = category;
        this.title = title;
        this.content = content;
        this.countryId = countryId;
        this.lectureId = lectureId;
        this.freeTags = freeTags;
        this.imageUrls = imageUrls;
        this.createdAt = LocalDateTime.now();
    }

    // DB 조회 후 재구성용 생성자
    private Post(Long id, Long authorId, PostTagType category, String title, String content,
                 Long countryId, Long lectureId, List<String> freeTags, List<String> imageUrls,
                 LocalDateTime createdAt, Integer viewCount, Boolean isDeleted) {
        this.id = id;
        this.authorId = authorId;
        this.category = category;
        this.title = title;
        this.content = content;
        this.countryId = countryId;
        this.lectureId = lectureId;
        this.freeTags = freeTags;
        this.imageUrls = imageUrls;
        this.createdAt = createdAt;
        this.viewCount = viewCount;
        this.isDeleted = isDeleted;
    }

    // 2. 정적 팩토리 메서드
    // 게시글 생성
    public static Post create(Long authorId, PostTagType category, String title, String content,
                              Long countryId, Long lectureId, List<String> freeTags, List<String> imageUrls) {


        return new Post(authorId, category, title, content, countryId, lectureId, freeTags, imageUrls);
    }

    // 게시글 수정
    public void update(Long requesterId, PostTagType category, String title, String content,
                       Long countryId, Long lectureId, List<String> freeTags, List<String> imageUrls) {
        // 권한 검증
        validateOwnerForUpdate(requesterId);

        // 비즈니스 규칙 검증
        validateCategory(category);
        validateTitle(title);
        validateContent(content);
        validateFreeTags(freeTags);
        validateImages(imageUrls);

        this.category = category;
        this.title = title;
        this.content = content;
        this.countryId = countryId;
        this.lectureId = lectureId;
        this.freeTags = freeTags;
        this.imageUrls = imageUrls;
    }

    // 조회수 증가
    public void increaseViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    private void validateOwnerForUpdate(Long requesterId) {
        if (!this.authorId.equals(requesterId)) {
            throw new PostException(PostErrorCode.POST_UPDATE_FORBIDDEN);
        }
    }

    private void validateOwnerForDelete(Long requesterId) {
        if (!this.authorId.equals(requesterId)) {
            throw new PostException(PostErrorCode.POST_DELETE_FORBIDDEN);
        }
    }

    // 게시글 삭제
    public void delete(Long requesterId) {
        validateOwnerForDelete(requesterId);
        this.isDeleted = true;
    }


    public static Post reconstitute(Long id, Long authorId, PostTagType category, String title, String content,
                                    Long countryId, Long lectureId, List<String> freeTags, List<String> imageUrls,
                                    LocalDateTime createdAt, Integer viewCount, Boolean isDeleted) {
        return new Post(id, authorId, category, title, content, countryId, lectureId,
                freeTags, imageUrls, createdAt, viewCount, isDeleted);
    }

    // 3. 도메인 규칙 검증 메서드 (상단 import에 맞게 BusinessException으로 일관성 유지)
    private void validateCategory(PostTagType category) {
        if (category == null) {
            throw new PostException(PostErrorCode.POST_CATEGORY_INVALID);
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new PostException(PostErrorCode.POST_TITLE_BLANK);
        }
    }

    private void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new PostException(PostErrorCode.POST_CONTENT_BLANK);
        }
    }

    private void validateFreeTags(List<String> freeTags) {
        if (freeTags != null && freeTags.size() > MAX_FREE_TAG_COUNT) {
            throw new PostException(PostErrorCode.POST_FREE_TAG_LIMIT_EXCEEDED);
        }
        // 중복 검증
        if (freeTags != null) {
            long distinctCount = freeTags.stream().distinct().count();
            if (distinctCount != freeTags.size()) {
                throw new PostException(PostErrorCode.POST_FREE_TAG_DUPLICATED);
            }
            // 태그 길이 검증 추가
            boolean hasLongTag = freeTags.stream().anyMatch(tag -> tag.length() > 10);
            if (hasLongTag) {
                throw new PostException(PostErrorCode.POST_FREE_TAG_TOO_LONG);
            }
        }
    }

    private void validateImages(List<String> imageUrls) {
        if (imageUrls != null && imageUrls.size() > MAX_IMAGE_COUNT) {
            throw new PostException(PostErrorCode.POST_IMAGE_COUNT_EXCEEDED);
        }
    }
}