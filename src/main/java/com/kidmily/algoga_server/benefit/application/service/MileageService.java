package com.kidmily.algoga_server.benefit.application.service;

import com.kidmily.algoga_server.benefit.application.command.AdminMileageTransactionCommand;
import com.kidmily.algoga_server.benefit.application.command.RewardReferralSignupCommand;
import com.kidmily.algoga_server.benefit.application.port.LmsCoursePort;
import com.kidmily.algoga_server.benefit.application.port.UserProfilePort;
import com.kidmily.algoga_server.benefit.application.result.AdminMileageHistoryResult;
import com.kidmily.algoga_server.benefit.application.result.AdminMileageSummaryResult;
import com.kidmily.algoga_server.benefit.application.result.AdminMileageUserResult;
import com.kidmily.algoga_server.benefit.application.usecase.MileageUseCase;
import com.kidmily.algoga_server.benefit.domain.model.MileageHistory;
import com.kidmily.algoga_server.benefit.domain.model.ReferralReward;
import com.kidmily.algoga_server.benefit.domain.repository.MileageHistoryRepository;
import com.kidmily.algoga_server.benefit.domain.repository.ReferralRewardRepository;
import com.kidmily.algoga_server.benefit.exception.BenefitErrorCode;
import com.kidmily.algoga_server.benefit.exception.BenefitException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MileageService implements MileageUseCase {

    private static final int REFERRAL_REWARD_MILEAGE = 3000;
    private static final String REFERRAL_REWARD_REASON = "추천인 회원가입 보상";

    private final MileageHistoryRepository mileageHistoryRepository;
    private final ReferralRewardRepository referralRewardRepository;
    private final LmsCoursePort lmsCoursePort;
    private final UserProfilePort userProfilePort;

    @Override
    public AdminMileageSummaryResult getMileageUsers(Pageable pageable) {
        log.info("[Admin Mileage Query] 사용자별 마일리지 목록 조회 요청. page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        LocalDateTime now = LocalDateTime.now();
        MileageHistoryRepository.GlobalMileageTotals totals = mileageHistoryRepository.findGlobalTotals(now);

        Page<Long> userIdPage = mileageHistoryRepository.findDistinctUserIds(pageable);
        List<Long> userIds = userIdPage.getContent();

        Map<Long, UserProfilePort.UserProfile> profiles = userProfilePort.findProfiles(userIds);
        Map<Long, List<MileageHistory>> historiesByUser = mileageHistoryRepository.findByUserIdIn(userIds)
                .stream()
                .collect(Collectors.groupingBy(MileageHistory::getUserId));

        List<AdminMileageUserResult> users = userIds.stream()
                .map(userId -> buildUserResult(profiles.get(userId), historiesByUser.getOrDefault(userId, List.of()), now))
                .flatMap(Optional::stream)
                .toList();

        Page<AdminMileageUserResult> userPage = new PageImpl<>(users, pageable, userIdPage.getTotalElements());
        int totalMileage = Math.max(0, totals.totalEarnedMileage() - totals.totalUsedMileage());

        log.info("[Admin Mileage Query] 사용자별 마일리지 목록 조회 완료. totalUserCount={}, pageUserCount={}",
                totals.userCount(), users.size());

        return new AdminMileageSummaryResult(
                totals.userCount(),
                totalMileage,
                totals.totalEarnedMileage(),
                totals.totalUsedMileage(),
                userPage
        );
    }

    @Override
    public Page<AdminMileageHistoryResult> getUserMileageHistories(Long userId, Pageable pageable) {
        log.info("[Admin Mileage Query] 사용자 마일리지 상세 조회 요청. userId={}, page={}, size={}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        validateUser(userId);

        Page<MileageHistory> historyPage = mileageHistoryRepository.findByUserId(userId, pageable);

        UserProfilePort.UserProfile profile = userProfilePort.findProfile(userId).orElse(null);
        Map<Long, LmsCoursePort.CourseSummary> courseSummaries = lmsCoursePort.findCourseSummaries(
                historyPage.getContent().stream()
                        .map(MileageHistory::getCourseId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList()
        );

        Page<AdminMileageHistoryResult> results = historyPage.map(history -> toHistoryResult(
                history,
                profile,
                Optional.ofNullable(courseSummaries.get(history.getCourseId()))
                        .map(LmsCoursePort.CourseSummary::courseTitle)
                        .orElse(null)
        ));

        log.info("[Admin Mileage Query] 사용자 마일리지 상세 조회 완료. userId={}, totalCount={}",
                userId, results.getTotalElements());

        return results;
    }

    @Override
    @Transactional
    public AdminMileageHistoryResult earnMileage(AdminMileageTransactionCommand command) {
        log.info("[Admin Mileage Command] 마일리지 지급 요청. userId={}, managerId={}, amount={}",
                command.userId(), command.managerId(), command.amount());

        validateUser(command.userId());
        validateAmount(command.amount());

        MileageHistory history = MileageHistory.adminEarn(
                command.userId(),
                command.managerId(),
                command.amount(),
                command.reason()
        );

        MileageHistory savedHistory = mileageHistoryRepository.save(history);

        log.info("[Admin Mileage Command] 마일리지 지급 완료. mileageHistoryId={}, userId={}, amount={}",
                savedHistory.getId(), savedHistory.getUserId(), savedHistory.getAmount());

        return toSingleHistoryResult(savedHistory);
    }

    @Override
    @Transactional
    public AdminMileageHistoryResult useMileage(AdminMileageTransactionCommand command) {
        log.info("[Admin Mileage Command] 마일리지 회수 요청. userId={}, managerId={}, amount={}",
                command.userId(), command.managerId(), command.amount());

        validateUser(command.userId());
        validateAmount(command.amount());

        int currentMileage = calculateCurrentMileage(command.userId());

        if (currentMileage < command.amount()) {
            log.warn("[Admin Mileage Command] 마일리지 회수 실패. 보유 마일리지 부족. userId={}, currentMileage={}, requestAmount={}",
                    command.userId(), currentMileage, command.amount());
            throw new BenefitException(BenefitErrorCode.NOT_ENOUGH_MILEAGE);
        }

        MileageHistory history = MileageHistory.adminUse(
                command.userId(),
                command.managerId(),
                command.amount(),
                command.reason()
        );

        MileageHistory savedHistory = mileageHistoryRepository.save(history);

        log.info("[Admin Mileage Command] 마일리지 회수 완료. mileageHistoryId={}, userId={}, amount={}",
                savedHistory.getId(), savedHistory.getUserId(), savedHistory.getAmount());

        return toSingleHistoryResult(savedHistory);
    }

    @Override
    @Transactional
    public void rewardReferralSignup(RewardReferralSignupCommand command) {
        if (command.referrerUserId() == null || command.referredUserId() == null) {
            log.info("[ReferralReward] Skip referral reward. referredUserId={}, referrerUserId={}",
                    command.referredUserId(), command.referrerUserId());
            return;
        }

        if (command.referrerUserId().equals(command.referredUserId())) {
            log.warn("[ReferralReward] Self referral is not allowed. userId={}", command.referredUserId());
            return;
        }

        if (referralRewardRepository.existsByReferredUserId(command.referredUserId())) {
            log.info("[ReferralReward] Referral reward already exists. referredUserId={}", command.referredUserId());
            return;
        }

        try {
            ReferralReward referralReward = ReferralReward.create(
                    command.referrerUserId(),
                    command.referredUserId(),
                    REFERRAL_REWARD_MILEAGE
            );
            referralRewardRepository.save(referralReward);

            MileageHistory mileageHistory = MileageHistory.earn(
                    command.referrerUserId(),
                    null,
                    REFERRAL_REWARD_MILEAGE,
                    REFERRAL_REWARD_REASON
            );
            MileageHistory savedMileageHistory = mileageHistoryRepository.save(mileageHistory);

            log.info("[ReferralReward] Referral reward completed. referredUserId={}, referrerUserId={}, mileageHistoryId={}, amount={}",
                    command.referredUserId(), command.referrerUserId(), savedMileageHistory.getId(), REFERRAL_REWARD_MILEAGE);
        } catch (DataIntegrityViolationException exception) {
            log.info("[ReferralReward] Referral reward already processed by another transaction. referredUserId={}",
                    command.referredUserId());
        }
    }

    private Optional<AdminMileageUserResult> buildUserResult(
            UserProfilePort.UserProfile profile,
            List<MileageHistory> histories,
            LocalDateTime now
    ) {
        if (profile == null) {
            return Optional.empty();
        }

        int totalEarnedMileage = histories.stream()
                .filter(this::isEarnType)
                .filter(history -> history.isAvailableAt(now))
                .mapToInt(MileageHistory::getAmount)
                .sum();

        int totalUsedMileage = histories.stream()
                .filter(this::isUseType)
                .mapToInt(MileageHistory::getAmount)
                .sum();

        int totalMileage = Math.max(0, totalEarnedMileage - totalUsedMileage);

        LocalDateTime lastUpdatedAt = histories.stream()
                .map(MileageHistory::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return Optional.of(new AdminMileageUserResult(
                profile.userId(),
                profile.name(),
                profile.email(),
                totalMileage,
                totalEarnedMileage,
                totalUsedMileage,
                lastUpdatedAt
        ));
    }

    private AdminMileageHistoryResult toSingleHistoryResult(MileageHistory history) {
        UserProfilePort.UserProfile profile = userProfilePort.findProfile(history.getUserId()).orElse(null);
        return toHistoryResult(history, profile, findCourseTitle(history.getCourseId()));
    }

    private AdminMileageHistoryResult toHistoryResult(
            MileageHistory history,
            UserProfilePort.UserProfile profile,
            String courseTitle
    ) {
        String userName = profile == null ? null : profile.name();
        String userEmail = profile == null ? null : profile.email();

        int signedAmount = isUseType(history)
                ? history.getAmount() * -1
                : history.getAmount();

        String processorName = history.getManagerId() == null
                ? "시스템"
                : "관리자";

        return new AdminMileageHistoryResult(
                history.getId(),
                history.getUserId(),
                userName,
                userEmail,
                history.getCourseId(),
                courseTitle,
                history.getManagerId(),
                processorName,
                history.getAmount(),
                signedAmount,
                history.getType(),
                history.getReason(),
                history.getCreatedAt(),
                history.getExpiredAt()
        );
    }

    private String findCourseTitle(Long courseId) {
        if (courseId == null) {
            return null;
        }

        return lmsCoursePort.findCourseSummary(courseId)
                .map(LmsCoursePort.CourseSummary::courseTitle)
                .orElse(null);
    }

    private int calculateCurrentMileage(Long userId) {
        List<MileageHistory> histories = mileageHistoryRepository.findByUserId(userId);
        LocalDateTime now = LocalDateTime.now();

        int earned = histories.stream()
                .filter(this::isEarnType)
                .filter(history -> history.isAvailableAt(now))
                .mapToInt(MileageHistory::getAmount)
                .sum();

        int used = histories.stream()
                .filter(this::isUseType)
                .mapToInt(MileageHistory::getAmount)
                .sum();

        return Math.max(0, earned - used);
    }

    private boolean isEarnType(MileageHistory history) {
        return "EARN".equalsIgnoreCase(history.getType());
    }

    private boolean isUseType(MileageHistory history) {
        return "USE".equalsIgnoreCase(history.getType())
                || "USED".equalsIgnoreCase(history.getType());
    }

    private void validateUser(Long userId) {
        if (!userProfilePort.exists(userId)) {
            log.warn("[Admin Mileage] 마일리지 처리 실패. 사용자를 찾을 수 없습니다. userId={}", userId);
            throw new BenefitException(BenefitErrorCode.MILEAGE_USER_NOT_FOUND);
        }
    }

    private void validateAmount(int amount) {
        if (amount <= 0) {
            log.warn("[Admin Mileage] 마일리지 금액 검증 실패. amount={}", amount);
            throw new BenefitException(BenefitErrorCode.INVALID_MILEAGE_AMOUNT);
        }
    }
}
