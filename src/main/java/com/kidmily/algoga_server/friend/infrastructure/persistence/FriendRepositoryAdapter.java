package com.kidmily.algoga_server.friend.infrastructure.persistence;

import com.kidmily.algoga_server.friend.domain.model.FriendRelation;
import com.kidmily.algoga_server.friend.domain.model.RelationStatus;
import com.kidmily.algoga_server.friend.domain.repository.FriendRepository;
import com.kidmily.algoga_server.friend.infrastructure.mapper.FriendMapper;
import com.kidmily.algoga_server.friend.infrastructure.persistence.repository.SpringDataFriendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FriendRepositoryAdapter implements FriendRepository {
    private final SpringDataFriendRepository jpaRepository;
    private final FriendMapper mapper;

    @Override
    public FriendRelation save(FriendRelation relation) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(relation)));
    }

    @Override
    public Optional<FriendRelation> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Optional<FriendRelation> findRelationBetween(Long user1Id, Long user2Id) {
        return jpaRepository.findRelationBetween(user1Id, user2Id).map(mapper::toDomain);
    }

    @Override
    public long countAcceptedFriends(Long userId) {
        return jpaRepository.countAcceptedFriends(userId);
    }

    @Override
    public List<FriendRelation> findAcceptedFriends(Long userId) {
        return jpaRepository.findAcceptedFriends(userId).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<FriendRelation> findByReceiverIdAndStatus(Long receiverId, RelationStatus status) {
        return jpaRepository.findByReceiverIdAndStatus(receiverId, status).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<FriendRelation> findByRequesterIdAndStatus(Long requesterId, RelationStatus status) {
        return jpaRepository.findByRequesterIdAndStatus(requesterId, status).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<FriendRelation> findByRequesterIdAndReceiverId(Long requesterId, Long receiverId) {
        return jpaRepository.findByRequesterIdAndReceiverId(requesterId, receiverId).map(mapper::toDomain);
    }

    @Override
    public void delete(FriendRelation relation) {
        jpaRepository.delete(mapper.toEntity(relation));
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        jpaRepository.deleteAllByUserId(userId);
    }
}