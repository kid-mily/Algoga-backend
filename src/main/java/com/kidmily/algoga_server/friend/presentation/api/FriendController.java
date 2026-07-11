package com.kidmily.algoga_server.friend.presentation.api;

import com.kidmily.algoga_server.friend.application.command.CreateFriendCommand;
import com.kidmily.algoga_server.friend.application.usecase.FriendCommandUseCase;
import com.kidmily.algoga_server.friend.application.usecase.FriendQueryUseCase;
import com.kidmily.algoga_server.friend.application.usecase.FriendQueryUseCase.FriendView;
import com.kidmily.algoga_server.friend.exception.FriendErrorCode;
import com.kidmily.algoga_server.friend.exception.FriendException;
import com.kidmily.algoga_server.friend.presentation.api.request.CreateFriendRequest;
import com.kidmily.algoga_server.friend.presentation.api.response.FriendResponse;
import com.kidmily.algoga_server.global.common.api.response.ApiResponse;
import com.kidmily.algoga_server.user.settings.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Friend", description = "친구 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class FriendController {

    private final FriendCommandUseCase commandUseCase;
    private final FriendQueryUseCase queryUseCase;

    @Operation(summary = "내 친구 목록 정렬 조회", description = "닉네임 순으로 정렬된 친구 목록을 조회합니다.")
    @GetMapping("/friends")
    public ApiResponse<List<FriendResponse>> getFriends(@AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) throw new FriendException(FriendErrorCode.USER_NOT_FOUND);

        List<FriendView> views = queryUseCase.getFriends(user.getUser().getId());
        List<FriendResponse> response = views.stream().map(FriendResponse::from).collect(Collectors.toList());

        return ApiResponse.success("FRIEND_LIST_SUCCESS", "친구 목록을 조회했습니다.", response);
    }

    @Operation(summary = "받은 친구 요청 목록 조회", description = "나에게 들어온 친구 요청을 조회합니다.")
    @GetMapping("/friends/requests/received")
    public ApiResponse<List<FriendResponse>> getReceivedRequests(@AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) throw new FriendException(FriendErrorCode.USER_NOT_FOUND);

        List<FriendView> views = queryUseCase.getReceivedRequests(user.getUser().getId());
        List<FriendResponse> response = views.stream().map(FriendResponse::from).collect(Collectors.toList());

        return ApiResponse.success("FRIEND_REQUEST_LIST_SUCCESS", "받은 요청 목록을 조회했습니다.", response);
    }

    @Operation(summary = "사용자 코드로 친구 검색", description = "개인 번호로 상대방을 검색합니다.")
    @GetMapping("/users/search")
    public ApiResponse<FriendResponse> searchUserByCode(@RequestParam String code) {
        FriendView view = queryUseCase.searchUserByCode(code);
        return ApiResponse.success("USER_SEARCH_SUCCESS", "유저를 검색했습니다.", FriendResponse.from(view));
    }

    @Operation(summary = "친구 추가 요청")
    @PostMapping("/friends/requests")
    public ApiResponse<Void> sendFriendRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateFriendRequest request) {

        if (userDetails == null) throw new FriendException(FriendErrorCode.USER_NOT_FOUND);

        CreateFriendCommand command = new CreateFriendCommand(
                userDetails.getUser().getId(),
                request.targetUserCode()
        );
        commandUseCase.sendFriendRequest(command);

        return ApiResponse.success("FRIEND_REQUEST_SUCCESS", "친구 요청을 보냈습니다.");
    }

    @Operation(summary = "친구 요청 수락", description = "받은 친구 요청을 수락합니다.")
    @PatchMapping("/friends/requests/{request_id}/accept")
    public ApiResponse<Void> acceptFriendRequest(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable("request_id") Long requestId) {

        if (user == null) throw new FriendException(FriendErrorCode.USER_NOT_FOUND);

        commandUseCase.acceptFriendRequest(user.getUser().getId(), requestId);
        return ApiResponse.success("FRIEND_ACCEPT_SUCCESS", "친구 요청을 수락했습니다.");
    }

    @Operation(summary = "친구 요청 거절", description = "받은 친구 요청을 거절하고 삭제합니다.")
    @PatchMapping("/friends/requests/{request_id}/reject")
    public ApiResponse<Void> rejectFriendRequest(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable("request_id") Long requestId) {

        if (user == null) throw new FriendException(FriendErrorCode.USER_NOT_FOUND);

        commandUseCase.rejectFriendRequest(user.getUser().getId(), requestId);
        return ApiResponse.success("FRIEND_REJECT_SUCCESS", "친구 요청을 거절했습니다.");
    }

    @Operation(summary = "친구 삭제", description = "현재 맺고 있는 친구 관계를 삭제합니다.")
    @DeleteMapping("/friends/{relationId}")
    public ApiResponse<Void> deleteFriend(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long relationId) {

        if (userDetails == null) throw new FriendException(FriendErrorCode.USER_NOT_FOUND);

        commandUseCase.deleteFriend(userDetails.getUser().getId(), relationId);
        return ApiResponse.success("FRIEND_DELETE_SUCCESS", "친구를 삭제했습니다.");
    }

    @Operation(summary = "유저 차단", description = "상대방 유저를 차단합니다.")
    @PostMapping("/friends/blocks")
    public ApiResponse<Void> blockUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateFriendRequest request) {

        if (userDetails == null) throw new FriendException(FriendErrorCode.USER_NOT_FOUND);

        CreateFriendCommand command = new CreateFriendCommand(
                userDetails.getUser().getId(),
                request.targetUserCode()
        );
        commandUseCase.blockUser(command);

        return ApiResponse.success("USER_BLOCK_SUCCESS", "유저를 차단했습니다.");
    }

    @Operation(summary = "유저 차단 해제", description = "차단했던 유저의 차단 상태를 해제합니다.")
    @DeleteMapping("/friends/blocks/{targetUserCode}")
    public ApiResponse<Void> unblockUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String targetUserCode) {

        if (userDetails == null) throw new FriendException(FriendErrorCode.USER_NOT_FOUND);

        commandUseCase.unblockUser(userDetails.getUser().getId(), targetUserCode);
        return ApiResponse.success("USER_UNBLOCK_SUCCESS", "차단을 해제했습니다.");
    }
}