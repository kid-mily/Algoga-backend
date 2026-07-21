package com.kidmily.algoga_server.community.application.service;

import com.kidmily.algoga_server.community.application.command.AdminDeletePostCommand;
import com.kidmily.algoga_server.community.application.command.CreatePostCommand;
import com.kidmily.algoga_server.community.application.command.DeletePostCommand;
import com.kidmily.algoga_server.community.application.command.UpdatePostCommand;
import com.kidmily.algoga_server.community.domain.model.Comment;
import com.kidmily.algoga_server.community.domain.model.Post;
import com.kidmily.algoga_server.community.domain.model.PostTagType;
import com.kidmily.algoga_server.community.domain.repository.CommentRepository;
import com.kidmily.algoga_server.community.domain.repository.PostRepository;
import com.kidmily.algoga_server.community.exception.PostErrorCode;
import com.kidmily.algoga_server.community.exception.PostException;
import com.kidmily.algoga_server.community.settings.CommunityStorageSettings;
import com.kidmily.algoga_server.global.port.out.FileStoragePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostCommandServiceTest {

    @InjectMocks
    private PostCommandService postCommandService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private FileStoragePort fileStoragePort;

    @Mock
    private CommunityStorageSettings storageSettings;

    private static final Long AUTHOR_ID = 1L;
    private static final Long POST_ID = 100L;

    private Post samplePost(Long authorId) {
        return Post.reconstitute(
                POST_ID, authorId, PostTagType.QUESTION, "제목", "본문",
                null, null, List.of(), List.of(),
                null, 0, false, null
        );
    }

    @Test
    @DisplayName("게시글 작성 시 이미지가 없으면 업로드 없이 도메인을 저장하고 ID를 반환한다.")
    void createPost_withoutImage_success() {
        // given
        CreatePostCommand command = new CreatePostCommand(
                AUTHOR_ID, PostTagType.QUESTION, "제목", "본문",
                null, null, List.of(), null
        );

        given(postRepository.save(any(Post.class))).willReturn(samplePost(AUTHOR_ID));

        // when
        Long result = postCommandService.handle(command);

        // then
        assertThat(result).isEqualTo(POST_ID);
        verify(fileStoragePort, never()).uploadFile(any(), anyString());
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 작성 시 이미지가 있으면 스토리지에 업로드하고 도메인을 저장한다.")
    void createPost_withImage_success() {
        // given
        MockMultipartFile file = new MockMultipartFile("images", "test.jpg", "image/jpeg", "data".getBytes());
        CreatePostCommand command = new CreatePostCommand(
                AUTHOR_ID, PostTagType.QUESTION, "제목", "본문",
                null, null, List.of(), List.of(file)
        );

        given(storageSettings.getDirectory()).willReturn("community-dir");
        given(fileStoragePort.uploadFile(any(), anyString())).willReturn("community/test.jpg");
        given(postRepository.save(any(Post.class))).willReturn(samplePost(AUTHOR_ID));

        // when
        Long result = postCommandService.handle(command);

        // then
        assertThat(result).isEqualTo(POST_ID);
        verify(fileStoragePort, times(1)).uploadFile(any(MultipartFile.class), anyString());
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 삭제 시 연관 댓글을 소프트 딜리트하고 게시글을 삭제한다.")
    void deletePost_success() {
        // given
        DeletePostCommand command = new DeletePostCommand(POST_ID, AUTHOR_ID);
        Post post = samplePost(AUTHOR_ID);
        List<Comment> comments = List.of();

        given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));
        given(commentRepository.findAllByPostId(POST_ID)).willReturn(comments);

        // when
        postCommandService.handle(command);

        // then
        verify(commentRepository, times(1)).softDeleteAll(comments);
        verify(postRepository, times(1)).delete(post);
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 삭제하면 예외가 발생한다.")
    void deletePost_notFound() {
        // given
        DeletePostCommand command = new DeletePostCommand(POST_ID, AUTHOR_ID);
        given(postRepository.findById(POST_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postCommandService.handle(command))
                .isInstanceOf(PostException.class)
                .hasMessage(PostErrorCode.POST_NOT_FOUND.getMessage());

        verify(postRepository, never()).delete(any());
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 게시글을 삭제하면 권한 예외가 발생한다.")
    void deletePost_forbidden() {
        // given
        Long otherUserId = 999L;
        DeletePostCommand command = new DeletePostCommand(POST_ID, otherUserId);
        Post post = samplePost(AUTHOR_ID);   // 작성자는 AUTHOR_ID

        given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));
        given(commentRepository.findAllByPostId(POST_ID)).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> postCommandService.handle(command))
                .isInstanceOf(PostException.class)
                .hasMessage(PostErrorCode.POST_DELETE_FORBIDDEN.getMessage());

        verify(postRepository, never()).delete(any());
    }

    @Test
    @DisplayName("관리자 삭제는 작성자가 아니어도 게시글을 삭제한다.")
    void adminDeletePost_success() {
        // given
        AdminDeletePostCommand command = new AdminDeletePostCommand(POST_ID);
        Post post = samplePost(AUTHOR_ID);

        given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));
        given(commentRepository.findAllByPostId(POST_ID)).willReturn(List.of());

        // when
        postCommandService.handle(command);

        // then
        verify(commentRepository, times(1)).softDeleteAll(any());
        verify(postRepository, times(1)).delete(post);
    }

    @Test
    @DisplayName("게시글 수정 시 작성자가 아니면 권한 예외가 발생한다.")
    void updatePost_forbidden() {
        // given
        Long otherUserId = 999L;
        UpdatePostCommand command = new UpdatePostCommand(
                POST_ID, otherUserId, PostTagType.QUESTION, "수정제목", "수정본문",
                null, null, List.of(), null
        );
        given(postRepository.findById(POST_ID)).willReturn(Optional.of(samplePost(AUTHOR_ID)));

        // when & then
        assertThatThrownBy(() -> postCommandService.handle(command))
                .isInstanceOf(PostException.class)
                .hasMessage(PostErrorCode.POST_UPDATE_FORBIDDEN.getMessage());

        verify(postRepository, never()).update(any());
    }
}