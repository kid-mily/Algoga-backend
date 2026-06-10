package com.kidmily.algoga_server.community.application.service;

import com.kidmily.algoga_server.community.application.command.CreatePostCommand;
import com.kidmily.algoga_server.community.application.command.DeletePostCommand;
import com.kidmily.algoga_server.community.application.command.UpdatePostCommand;
import com.kidmily.algoga_server.community.application.usecase.PostCommandUseCase;
import com.kidmily.algoga_server.community.domain.model.Comment;
import com.kidmily.algoga_server.community.domain.model.Post;
import com.kidmily.algoga_server.community.domain.repository.CommentRepository;
import com.kidmily.algoga_server.community.domain.repository.PostRepository;
import com.kidmily.algoga_server.community.exception.PostErrorCode;
import com.kidmily.algoga_server.community.exception.PostException;
import com.kidmily.algoga_server.community.settings.CommunityStorageSettings;
import com.kidmily.algoga_server.global.port.out.FileStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PostCommandService implements PostCommandUseCase {

    private final PostRepository postRepository;
    private final FileStoragePort fileStoragePort;
    private final CommunityStorageSettings storageSettings;
    private final CommentRepository commentRepository;



    @Override
    public Long handle(CreatePostCommand command) {

        // 받은 정보 확인 로그
        log.info("[PostCommandService] 게시글 작성 요청 수신 - authorId: {}, category: {}, title: {}, freeTagsCount: {}, imageCount: {}",
                command.authorId(),
                command.category(),
                command.title(),
                command.freeTags() == null ? 0 : command.freeTags().size(),
                command.images() == null ? 0 : command.images().size());

// 🌟 1. 파일 리스트가 존재하면 S3(MinIO)에 차례대로 업로드하고 URL 리스트 생성
        List<String> imageUrls = new ArrayList<>();
        if (command.images() != null && !command.images().isEmpty()) {
            imageUrls = command.images().stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .map(file -> fileStoragePort.uploadFile(
                            file,
                            storageSettings.getBucketName(),
                            storageSettings.getDirectory()
                    ))
                    .toList();
        }

        Post newPost = Post.create(

                command.authorId(),
                command.category(),
                command.title(),
                command.content(),
                command.countryId(),
                command.lectureId(),
                command.freeTags(),
                imageUrls
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

        // 🌟 1. 수정할 이미지 리스트의 기본값은 기존 게시글의 이미지 URL 리스트로 설정
        List<String> targetImageUrls = new ArrayList<>(post.getImageUrls());

        // 🌟 2. 새 이미지 파일들이 업로드되어 들어왔다면 기존 S3 파일을 지우고 교체 작업 진행 (배너 방식 적용)
        if (command.images() != null && !command.images().isEmpty()) {
            // 기존 S3 파일 전부 삭제
            post.getImageUrls().forEach(oldUrl ->
                    fileStoragePort.deleteFile(storageSettings.getBucketName(), oldUrl)
            );
            targetImageUrls.clear();

            // 새 S3 파일 전체 업로드 및 URL 저장
            targetImageUrls = command.images().stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .map(file -> fileStoragePort.uploadFile(
                            file,
                            storageSettings.getBucketName(),
                            storageSettings.getDirectory()
                    ))
                    .toList();
        }

        post.update(
                command.requesterId(),
                command.category(),
                command.title(),
                command.content(),
                command.countryId(),
                command.lectureId(),
                command.freeTags(),
                targetImageUrls
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

        // 연관 댓글 및 대댓글 소프트 딜리트
        List<Comment> comments = commentRepository.findAllByPostId(command.postId());
        commentRepository.softDeleteAll(comments);

        post.delete(command.requesterId());
        postRepository.delete(post);

        log.info("[PostCommandService] 게시글 삭제 완료 - postId: {}", command.postId());
    }
}
