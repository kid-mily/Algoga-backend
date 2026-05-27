package com.kidmily.algoga_server.friend.application.service;

import com.kidmily.algoga_server.friend.application.usecase.FriendQueryUseCase;
import com.kidmily.algoga_server.friend.domain.model.FriendRelation;
import com.kidmily.algoga_server.friend.domain.model.RelationStatus;
import com.kidmily.algoga_server.friend.domain.repository.FriendRepository;
import com.kidmily.algoga_server.friend.exception.FriendErrorCode;
import com.kidmily.algoga_server.friend.exception.FriendException;
import com.kidmily.algoga_server.friend.presentation.api.response.FriendResponse;
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
public class FriendQueryService implements FriendQueryUseCase {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    @Override
    public List<FriendResponse> getFriends(Long myId) {
        List<FriendRelation> relations = friendRepository.findAcceptedFriends(myId);

        return relations.stream().map(relation -> {
                    Long friendUserId = relation.getRequesterId().equals(myId) ? relation.getReceiverId() : relation.getRequesterId();
                    User friend = userRepository.findById(friendUserId).orElseThrow();
                    return FriendResponse.of(relation.getId(), friend);
                })
                .sorted((a, b) -> a.nickname().compareToIgnoreCase(b.nickname())) // 닉네임 순 정렬
                .collect(Collectors.toList());
    }

    @Override
    public List<FriendResponse> getReceivedRequests(Long myId) {
        List<FriendRelation> requests = friendRepository.findByReceiverIdAndStatus(myId, RelationStatus.REQUESTED);

        return requests.stream().map(req -> {
            User requester = userRepository.findById(req.getRequesterId()).orElseThrow();
            return FriendResponse.of(req.getId(), requester);
        }).collect(Collectors.toList());
    }

    @Override
    public FriendResponse searchUserByCode(String code) {
        User user = userRepository.findByPersonalCode(code)
                .orElseThrow(() -> new FriendException(FriendErrorCode.USER_NOT_FOUND));
        return FriendResponse.of(null, user);
    }
}