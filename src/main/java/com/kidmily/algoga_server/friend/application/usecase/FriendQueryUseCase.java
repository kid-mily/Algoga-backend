package com.kidmily.algoga_server.friend.application.usecase;

import com.kidmily.algoga_server.friend.presentation.api.response.FriendResponse;
import java.util.List;

public interface FriendQueryUseCase {
    List<FriendResponse> getFriends(Long myId);
    List<FriendResponse> getReceivedRequests(Long myId);
    FriendResponse searchUserByCode(String code);
}