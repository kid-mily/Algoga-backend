package com.kidmily.algoga_server.blacklist.application.usecase;

import com.kidmily.algoga_server.blacklist.application.query.GetBlacklistUserDetailQuery;
import com.kidmily.algoga_server.blacklist.application.query.GetBlacklistUsersQuery;
import com.kidmily.algoga_server.blacklist.presentation.api.response.BlacklistUserDetailResponse;
import com.kidmily.algoga_server.global.common.api.response.PageResponse;

public interface BlacklistQueryUseCase {
    
    // 1. 블랙리스트 후보 유저 전체 목록 페이징 조회
    PageResponse<BlacklistUserDetailResponse> getCandidateUserList(GetBlacklistUsersQuery query);
    
    // 2. 이미 블랙리스트에 등록된 유저 목록 페이징 조회
    PageResponse<BlacklistUserDetailResponse> getBlacklistedUserList(GetBlacklistUsersQuery query);
    
    // 3. 단일 유저 상세 조회
    BlacklistUserDetailResponse getUserDetail(GetBlacklistUserDetailQuery query);
    
}