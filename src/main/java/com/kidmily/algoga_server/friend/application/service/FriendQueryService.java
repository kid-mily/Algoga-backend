package com.kidmily.algoga_server.friend.application.service;

import com.kidmily.algoga_server.friend.application.usecase.FriendQueryUseCase;
import com.kidmily.algoga_server.friend.application.usecase.FriendQueryUseCase.FriendView;
import com.kidmily.algoga_server.friend.domain.model.FriendRelation;
import com.kidmily.algoga_server.friend.domain.model.RelationStatus;
import com.kidmily.algoga_server.friend.domain.repository.FriendRepository;
import com.kidmily.algoga_server.friend.exception.FriendErrorCode;
import com.kidmily.algoga_server.friend.exception.FriendException;
import com.kidmily.algoga_server.user.domain.User;
import com.kidmily.algoga_server.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class
FriendQueryService implements FriendQueryUseCase {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    @Override
    public List<FriendView> getFriends(Long myId) {
        List<FriendRelation> relations = friendRepository.findAcceptedFriends(myId);

        return relations.stream().map(relation -> {
                    Long friendUserId = relation.getRequesterId().equals(myId) ? relation.getReceiverId() : relation.getRequesterId();
                    User friend = userRepository.findById(friendUserId).orElseThrow();

                    return new FriendView(
                            relation.getId(),
                            friend.getId(),
                            friend.getNickname(),
                            friend.getPersonalCode(),
                            friend.getProfileImageUrl()
                    );
                })
                .sorted((a, b) -> a.nickname().compareToIgnoreCase(b.nickname()))
                .collect(Collectors.toList());
    }

    @Override
    public List<FriendView> getReceivedRequests(Long myId) {
        List<FriendRelation> requests = friendRepository.findByReceiverIdAndStatus(myId, RelationStatus.REQUESTED);

        return requests.stream().map(req -> {
            User requester = userRepository.findById(req.getRequesterId()).orElseThrow();
            return new FriendView(
                    req.getId(),
                    requester.getId(),
                    requester.getNickname(),
                    requester.getPersonalCode(),
                    requester.getProfileImageUrl()
            );
        }).collect(Collectors.toList());
    }

    @Override
    public FriendView searchUserByCode(String code) {
        User user = userRepository.findByPersonalCode(code)
                .orElseThrow(() -> new FriendException(FriendErrorCode.USER_NOT_FOUND));

        return new FriendView(
                null,
                user.getId(),
                user.getNickname(),
                user.getPersonalCode(),
                user.getProfileImageUrl()
        );
    }
}