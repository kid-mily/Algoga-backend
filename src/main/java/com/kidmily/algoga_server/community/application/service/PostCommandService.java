package com.kidmily.algoga_server.community.application.service;

import com.kidmily.algoga_server.community.application.command.CreatePostCommand;
import com.kidmily.algoga_server.community.application.command.DeletePostCommand;
import com.kidmily.algoga_server.community.application.command.UpdatePostCommand;
import com.kidmily.algoga_server.community.application.usecase.PostCommandUseCase;
import com.kidmily.algoga_server.community.domain.model.Post;
import com.kidmily.algoga_server.community.domain.repository.PostRepository;
import com.kidmily.algoga_server.community.exception.PostErrorCode;
import com.kidmily.algoga_server.community.exception.PostException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PostCommandService implements PostCommandUseCase {

    private final PostRepository postRepository;


    @Override
    public Long handle(CreatePostCommand command) {

        // 받은 정보 확인 로그
        log.info("[PostCommandService] 게시글 작성 요청 수신 - authorId: {}, category: {}, title: {}, freeTagsCount: {}",
                command.authorId(),
                command.category(),
                command.title(),
                command.freeTags() == null ? 0 : command.freeTags().size());

        Post newPost = Post.create(

                command.authorId(),
                command.category(),
                command.title(),
                command.content(),
                command.countryId(),
                command.lectureId(),
                command.freeTags(),
                List.of()  // 이미지는 S3 구현 후 추가
        );


        Post savedPost = postRepository.save(newPost);

        log.info("[PostCommandService] 게시글 작성 완료 - postId: {}", savedPost.getId());

        return savedPost.getId();
    }

    @Override
    public Long handle(UpdatePostCommand command) {
        log.info("[PostCommandService] 게시글 수정 요청 수신 - postId: {}, requesterId: {}",
                command.postId(), command.requesterId());

        Post post = postRepository.findById(command.postId())
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        post.update(
                command.requesterId(),
                command.category(),
                command.title(),
                command.content(),
                command.countryId(),
                command.lectureId(),
                command.freeTags(),
                List.of()
        );

        Post updatedPost = postRepository.update(post);  // save → update

        log.info("[PostCommandService] 게시글 수정 완료 - postId: {}", updatedPost.getId());
        return updatedPost.getId();
    }

    @Override
    public void handle(DeletePostCommand command) {
        log.info("[PostCommandService] 게시글 삭제 요청 수신 - postId: {}, requesterId: {}",
                command.postId(), command.requesterId());

        Post post = postRepository.findById(command.postId())
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        post.delete(command.requesterId());
        postRepository.delete(post);

        log.info("[PostCommandService] 게시글 삭제 완료 - postId: {}", command.postId());
    }
}
