package com.kidmily.algoga_server.friend.application.usecase;

public interface FriendCommandUseCase {
    void sendFriendRequest(Long myId, String targetPersonalCode);

    void acceptFriendRequest(Long myId, Long requestId);

    void rejectFriendRequest(Long myId, Long requestId);

    void deleteFriend(Long myId, Long relationId);

    void blockUser(Long myId, String targetUserCode);

    void unblockUser(Long myId, String targetUserCode);
}