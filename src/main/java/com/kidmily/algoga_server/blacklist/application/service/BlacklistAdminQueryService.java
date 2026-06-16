package com.kidmily.algoga_server.blacklist.application.service;

import com.kidmily.algoga_server.blacklist.exception.BlacklistErrorCode;
import com.kidmily.algoga_server.blacklist.exception.BlacklistException;
import com.kidmily.algoga_server.blacklist.infrastructure.persistence.repository.BlacklistQueryRepository;
import com.kidmily.algoga_server.blacklist.presentation.api.response.BlacklistCandidateDetailResponse;
import com.kidmily.algoga_server.global.common.api.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlacklistAdminQueryService {

    private final BlacklistQueryRepository queryRepository;

    public PageResponse<BlacklistCandidateDetailResponse> getCandidateUserList(int page, int size) {
        PageRequest pageRequest = PageRequest.of(Math.max(0, page - 1), size);
        
        Page<BlacklistCandidateDetailResponse> result = queryRepository.findAllCandidateUsers(pageRequest)
                .map(BlacklistCandidateDetailResponse::from);

        return PageResponse.from(result);
    }

    public BlacklistCandidateDetailResponse getCandidateUserDetail(Long userId) {
        // 예외 처리 4: 후보 유저 상세조회 시 데이터 없으면 예외 발생
        return queryRepository.findCandidateUserDetailById(userId)
                .map(BlacklistCandidateDetailResponse::from)
                .orElseThrow(() -> new BlacklistException(BlacklistErrorCode.CANDIDATE_NOT_FOUND));
    }
}