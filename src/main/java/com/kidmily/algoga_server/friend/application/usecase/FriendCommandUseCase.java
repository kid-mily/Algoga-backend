package com.kidmily.algoga_server.friend.application.usecase;

import com.kidmily.algoga_server.friend.application.command.CreateFriendCommand;

public interface FriendCommandUseCase {
    void sendFriendRequest(CreateFriendCommand command);
    void acceptFriendRequest(Long myId, Long requestId);
    void rejectFriendRequest(Long myId, Long requestId);
    void deleteFriend(Long myId, Long relationId);
    void blockUser(CreateFriendCommand command);
    void unblockUser(Long myId, String targetUserCode);
}