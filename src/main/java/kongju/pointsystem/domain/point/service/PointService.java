package kongju.pointsystem.domain.point.service;

import java.time.LocalDateTime;
import java.util.*;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kongju.pointsystem.domain.point.entity.*;
import kongju.pointsystem.domain.user.entity.*;
import kongju.pointsystem.domain.point.dto.*;
import kongju.pointsystem.global.error.exception.*;
import kongju.pointsystem.domain.user.repository.*;
import kongju.pointsystem.domain.point.repository.*;


@Service
@RequiredArgsConstructor
public class PointService {

    private final UserRepository userRepository;
    private final PointDetailRepository pointDetailRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final PointUsageRepository pointUsageRepository;
    private final UserBalanceRepository userBalanceRepository;
    private final PointExpirationProcessor pointExpirationProcessor;

    /**
     * 유저 찾기, 없으면 에러 처리
     *
     * @param id 유저 아이디
     * @return 유저 객체
     */
    private User findByIdOrThrow(UUID id) {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    /**
     * 포인트 적립하는 서비스 로직
     *
     * @param request 사용자 id와 적립할 포인트
     */
    @Transactional
    public PointEarnResponse earnPoint(PointRequest request) {
        UUID userId = request.id();
        Long point = request.point();

        if (point <= 0) {
            throw new InvalidPointAmountException();
        }

        // 유저 발란스 가져오기
        UserBalance userBalance = findBalanceForUpdate(userId);
        // id로 유저 확인
        User user = userBalance.getUser();

        // 총 금액에 추가
        userBalance.earn(point);

        // 포인트 디테일 생성
        PointDetail pointdetail = PointDetail.builder()
                .amount(point)
                .user(user)
                .build();
        pointDetailRepository.save(pointdetail);

        // 포인트 히스토리 생성
        PointHistory pointHistory = PointHistory.builder()
                .type(PointType.EARN)
                .amount(point)
                .user(user)
                .build();
        pointHistoryRepository.save(pointHistory);

        userRepository.save(user);

        return PointEarnResponse.builder()
                .earnedAmount(point)
                .currentBalance(userBalance.getTotalAmount())
                .message("포인트가 적립되었습니다.")
                .build();

    }

    /**
     * 락 걸고 잔액 정보 가져옴
     *
     * @param userId 유저 아이디
     * @return 유저발란스
     */
    private @NonNull UserBalance findBalanceForUpdate(UUID userId) {
        return userBalanceRepository.findByUserIdWithLock(userId)
                .orElseThrow(UserBalanceNotFoundException::new);
    }

    /**
     * 포인트 조회
     *
     * @param request 사용자 id, 사용자가 확인할 날짜
     */
    @Transactional(readOnly = true)
    public PointResponse balancePoint(PointBalanceRequest request) {
        UUID userId = request.id();
        LocalDateTime expireTime = request.time();
        // id로 유저 확인
        User user = findByIdOrThrow(userId);

        // 유저 발란스 확인
        UserBalance userBalance = user.getUserBalance();

        if (expireTime == null) {
            return PointBalanceResponse.builder()
                    .balance(userBalance.getTotalAmount())
                    .build();
        }

        LocalDateTime now = LocalDateTime.now();
        List<PointDetail> pointDetailList = pointDetailRepository.findExpiringPointsBetween(userId, now, expireTime);

        Long totalAmount = pointDetailList
                .stream()
                .mapToLong(PointDetail::getRemainAmount)
                .sum();

        return PointBalanceExpireResponse.builder()
                .balance(totalAmount)
                .expiredAt(expireTime)
                .build();
    }

    /**
     * 포인트 사용
     *
     * @param request 사용할 유저 아이디, 사용할 포인트
     * @return
     */
    @Transactional
    public PointUseResponse usePoint(PointRequest request) {
        UUID userId = request.id();
        Long point = request.point();

        if (point <= 0) throw new InvalidPointAmountException();

        // 잔고 확인, 락 걸기
        UserBalance userBalance = findBalanceForUpdate(userId);

        User user = userBalance.getUser();

        // 포인트 디테일 불러오기
        LocalDateTime now = LocalDateTime.now();
        List<PointDetail> pointDetailList = pointDetailRepository.findRemainedDetailsNotExpired(userId, now);

        // 포인트 차감
        userBalance.use(point);

        // 포인트 차감
        UUID referenceId = UUID.randomUUID();
        Long originalPoint = point;

        // 포인트 히스토리 등록
        PointHistory pointHistory = PointHistory.builder()
                .type(PointType.USE)
                .referenceId(referenceId)
                .amount(originalPoint)
                .user(user)
                .build();

        pointHistoryRepository.save(pointHistory);

        List<PointUsage> pointUsages = new ArrayList<>();

        for (PointDetail pointDetail : pointDetailList) {
            if (point == 0) {
                break;
            }

            long consumedAmount = pointDetail.use(point);
            point -= consumedAmount;

            // 포인트 usage등록
            PointUsage pointUsage = PointUsage.builder()
                    .pointHistory(pointHistory)
                    .pointDetail(pointDetail)
                    .amount(consumedAmount)
                    .build();

            pointUsages.add(pointUsage);
        }

        pointUsageRepository.saveAll(pointUsages);

        return PointUseResponse.builder()
                .useAmount(originalPoint)
                .currentBalance(userBalance.getTotalAmount())
                .build();
    }

    /**
     * 포인트 환불
     *
     * @param request 유저 아이디, 환불 아이디
     * @return 환불된 포인트
     */
    @Transactional
    public PointRefundResponse refundPoint(RefundRequest request) {
        UUID userId = request.id();
        UUID referenceId = request.referenceId();
        // 유저 확인
        UserBalance userBalance = findBalanceForUpdate(userId);
        User user = userBalance.getUser();

        // 유효한 referenceId인지 확인
        boolean referenceIdExists = pointHistoryRepository.existsReferenceId(userId, referenceId);

        if (!referenceIdExists) {
            throw new RefundReferenceIdNotExsistException();
        }

        // 환불 여부 체크
        boolean isRefund = pointHistoryRepository.findRefundPoints(userId, referenceId);

        if (isRefund) {
            throw new RefundAlreadyProcessedException();
        }

        // 히스토리와 연관된 usage 찾기
        PointHistory pointHistory = pointHistoryRepository.findRefundablePoints(userId, referenceId);

        // usage와 연관된 detail찾기
        List<PointUsage> pointUsages = pointHistory.getPointUsages();
        Long refundAmount = 0L;

        LocalDateTime now = LocalDateTime.now();
        for (PointUsage pointUsage : pointUsages) {
            PointDetail pointDetail = pointUsage.getPointDetail();
            Long amount = pointUsage.getAmount();

            refundAmount += pointDetail.refund(amount, now);
        }

        PointHistory pointHistoryRefund = PointHistory.builder()
                .user(user)
                .amount(refundAmount)
                .type(PointType.REFUND)
                .referenceId(referenceId)
                .build();
        Long expiredAmount = pointHistory.getAmount() - refundAmount;

        if (expiredAmount > 0) {
            PointHistory pointHistoryExpired = PointHistory.builder()
                    .user(user)
                    .amount(expiredAmount)
                    .type(PointType.EXPIRE)
                    .referenceId(referenceId)
                    .build();
            pointHistoryRepository.save(pointHistoryExpired);
        }

        pointHistoryRepository.save(pointHistoryRefund);

        // 총 잔액 수정
        userBalance.refund(refundAmount);

        return PointRefundResponse.builder()
                .refundAmount(refundAmount)
                .currentBalance(userBalance.getTotalAmount())
                .expiredAmount(expiredAmount)
                .message("환불이 완료되었습니다.")
                .build();
    }

    /**
     * 포인트 만료
     */
    public void expirePoint() {
        // 만료 대상 user목록 조회
        LocalDateTime now = LocalDateTime.now();
        List<UUID> userIds = pointDetailRepository.findUsersWithExpiredPoints(now);

        userIds.forEach(userId -> {
            pointExpirationProcessor.expireUserPoints(userId, now);
        });
    }
}
