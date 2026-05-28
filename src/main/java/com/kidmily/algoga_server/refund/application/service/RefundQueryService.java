package com.kidmily.algoga_server.refund.application.service;

import com.kidmily.algoga_server.refund.application.usecase.RefundQueryUseCase;
import com.kidmily.algoga_server.refund.domain.model.RefundStatus;
import com.kidmily.algoga_server.refund.domain.repository.RefundRepository;
import com.kidmily.algoga_server.refund.presentation.api.response.RefundResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RefundQueryService implements RefundQueryUseCase {

    private final RefundRepository refundRepository;

    @Override
    public List<RefundResponse> getMyRefunds(Long userId) {
        log.warn("[RefundQueryService] 내 환불 목록 조회 - userId: {}", userId);
        return refundRepository.findAllByUserId(userId)
                .stream()
                .map(RefundResponse::from)
                .toList();
    }

    @Override
    public List<RefundResponse> getAllRefunds(RefundStatus status) {
        log.warn("[RefundQueryService] 전체 환불 목록 조회 - status: {}", status);
        if (status == null) {
            return refundRepository.findAll()
                    .stream()
                    .map(RefundResponse::from)
                    .toList();
        }
        return refundRepository.findAllByStatus(status)
                .stream()
                .map(RefundResponse::from)
                .toList();
    }
}