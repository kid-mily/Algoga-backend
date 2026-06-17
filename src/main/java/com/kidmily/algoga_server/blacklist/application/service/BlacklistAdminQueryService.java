package com.kidmily.algoga_server.blacklist.application.service;

import com.kidmily.algoga_server.blacklist.application.query.GetBlacklistUserDetailQuery;
import com.kidmily.algoga_server.blacklist.application.query.GetBlacklistUsersQuery;
import com.kidmily.algoga_server.blacklist.application.usecase.BlacklistQueryUseCase;
import com.kidmily.algoga_server.blacklist.domain.repository.BlacklistQueryRepository; // 🌟 Domain Repository 로 변경
import com.kidmily.algoga_server.blacklist.exception.BlacklistErrorCode;
import com.kidmily.algoga_server.blacklist.exception.BlacklistException;
import com.kidmily.algoga_server.blacklist.presentation.api.response.BlacklistUserDetailResponse;
import com.kidmily.algoga_server.global.common.api.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlacklistAdminQueryService implements BlacklistQueryUseCase {

    private final BlacklistQueryRepository queryRepository;

    @Override
    public PageResponse<BlacklistUserDetailResponse> getCandidateUserList(GetBlacklistUsersQuery query) {
        String safeKeyword = (query.keyword() == null) ? "" : query.keyword();
        PageRequest pageRequest = PageRequest.of(Math.max(0, query.page() - 1), query.size());
        
        Page<BlacklistUserDetailResponse> result = queryRepository.findCandidateUsers(safeKeyword, pageRequest);

        return PageResponse.from(result);
    }

    @Override
    public PageResponse<BlacklistUserDetailResponse> getBlacklistedUserList(GetBlacklistUsersQuery query) {
        String safeKeyword = (query.keyword() == null) ? "" : query.keyword();
        PageRequest pageRequest = PageRequest.of(Math.max(0, query.page() - 1), query.size());
        
        Page<BlacklistUserDetailResponse> result = queryRepository.findBlacklistedUsers(safeKeyword, pageRequest);

        return PageResponse.from(result);
    }

    @Override
    public BlacklistUserDetailResponse getUserDetail(GetBlacklistUserDetailQuery query) {
        return queryRepository.findUserDetailById(query.userId())
                .orElseThrow(() -> new BlacklistException(BlacklistErrorCode.CANDIDATE_NOT_FOUND));
    }
}