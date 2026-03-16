package kongju.pointsystem.domain.point.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import kongju.pointsystem.domain.point.entity.PointDetail;
import kongju.pointsystem.domain.point.entity.PointHistory;
import kongju.pointsystem.domain.point.entity.PointType;
import kongju.pointsystem.domain.user.entity.User;
import kongju.pointsystem.domain.user.entity.UserBalance;
import kongju.pointsystem.domain.user.repository.UserRepository;
import kongju.pointsystem.domain.point.dto.*;
import kongju.pointsystem.domain.point.entity.PointUsage;
import kongju.pointsystem.domain.point.repository.PointUsageRepository;
import kongju.pointsystem.global.error.exception.*;
import kongju.pointsystem.domain.point.repository.PointDetailRepository;
import kongju.pointsystem.domain.point.repository.PointHistoryRepository;
import kongju.pointsystem.domain.user.repository.UserBalanceRepository;


@Service
@RequiredArgsConstructor
@Transactional
public class PointService {

    private final UserBalanceRepository balanceRepository;
    private final UserRepository userRepository;
    private final PointDetailRepository pointDetailRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final UserBalanceRepository userBalanceRepository;
    private final PointUsageRepository pointUsageRepository;

    /**
     * 포인트 적립하는 서비스 로직
     *
     * @param request 사용자 id와 적립할 포인트
     */
    @Transactional
    public PointEarnResponse earnPoint(PointRequest request) {
        UUID userId = request.id();
        Long point = request.point();

        // id로 유저 확인
        User user = userRepository
                .findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 유효한 포인트인지 확인
        if (point <= 0) {
            throw new PointInvalidException();
        }

        // 유저 발란스가 있는지 확인 or 없으면 생성
        UserBalance balance = balanceRepository.findByUserIdWithLock(userId)
                .orElseGet(() -> UserBalance.builder()
                        .user(user)
                        .totalAmount(0L)
                        .build());
        balanceRepository.save(balance);

        // 포인트 디테일 생성
        PointDetail pointdetail = PointDetail.builder()
                .expiredAt(LocalDateTime.now().plusMonths(1))
                .amount(point)
                .remainAmount(point)
                .user(user)
                .build();
        pointDetailRepository.save(pointdetail);

        balance.setTotalAmount(balance.getTotalAmount() + point);
        // 포인트 히스토리 생성
        PointHistory pointHistory = PointHistory.builder()
                .type(PointType.EARN)
                .amount(point)
                .user(user)
                .build();
        pointHistoryRepository.save(pointHistory);

        return PointEarnResponse.builder()
                .earnedAmount(point)
                .currentBalance(balance.getTotalAmount())
                .message("포인트가 적립되었습니다.")
                .build();

    }

    /**
     * 포인트 조회
     *
     * @param request 사용자 id, 사용자가 확인할 날짜
     */
    @Transactional(readOnly = true)
    public PointResponse balancePoint(PointBalanceRequest request) {
        UUID userId = request.id();
        LocalDateTime time = request.time();
        // id로 유저 확인
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 유저 발란스가 있는지 확인 or 없으면 생성
        UserBalance balance = balanceRepository.findByUserIdWithLock(userId)
                .orElseGet(() -> UserBalance.builder()
                        .user(user)
                        .totalAmount(0L)
                        .build());

        if (time == null) {
            return PointBalanceResponse.builder()
                    .balance(balance.getTotalAmount())
                    .build();
        }

        List<PointDetail> pointDetailList = pointDetailRepository.findByPointExpire(userId, time);
        Long totalAmount = pointDetailList
                .stream()
                .mapToLong(PointDetail::getRemainAmount)
                .sum();

        return PointBalanceExpireResponse.builder()
                .balance(totalAmount)
                .expiredAt(time)
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
        // 유효 포인트인지 확인
        if (point <= 0) {
            throw new PointInvalidException();
        }
        // 유저 확인
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 잔고 확인
        UserBalance userBalance = userBalanceRepository.findByUserIdWithLock(userId)
                .orElseGet(() -> UserBalance.builder()
                        .user(user)
                        .totalAmount(0L)
                        .build());

        Long totalAmount = userBalance.getTotalAmount();
        // 총 금액 차감
        if (totalAmount < point) {
            throw new BalanceNotEnoughException();
        }
        userBalance.setTotalAmount(totalAmount - point);

        // 포인트 디테일 불러오기
        LocalDateTime now = LocalDateTime.now();
        List<PointDetail> pointDetailList = pointDetailRepository.findRemainedDetailsNotExpired(userId, now);

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

        for (PointDetail pointDetail : pointDetailList) {
            if (point == 0) {
                break;
            }

            Long remainAmount = pointDetail.getRemainAmount();
            long consumedAmount = Math.min(remainAmount, point);

            pointDetail.setRemainAmount(remainAmount - consumedAmount);
            point -= consumedAmount;

            // 포인트 usage등록
            PointUsage pointUsage = PointUsage.builder()
                    .pointHistory(pointHistory)
                    .pointDetail(pointDetail)
                    .amount(consumedAmount)
                    .build();

            pointUsageRepository.save(pointUsage);
        }

        return PointUseResponse.builder()
                .useAmount(originalPoint)
                .currentBalance(userBalance.getTotalAmount())
                .build();
    }

    @Transactional
    public PointRefundResponse refundPoint(RefundRequest request) {
        UUID userId = request.id();
        UUID referenceId = request.referenceId();
        // 유저 확인
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

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
        LocalDateTime now = LocalDateTime.now();
        Long refundAmount = 0L;

        for (PointUsage pointUsage : pointUsages) {
            PointDetail pointDetail = pointUsage.getPointDetail();
            LocalDateTime expiredAt = pointDetail.getExpiredAt();
            Long amount = pointUsage.getAmount();
            if (expiredAt.isAfter(now)) {
                pointDetail.setAmount(pointDetail.getAmount() + amount);
                refundAmount += amount;
            }
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
        UserBalance userBalance = userBalanceRepository.findByUserId(userId);
        Long totalAmount = userBalance.getTotalAmount();
        userBalance.setTotalAmount(totalAmount + refundAmount);

        return PointRefundResponse.builder()
                .refundAmount(refundAmount)
                .currentBalance(userBalance.getTotalAmount())
                .expiredAmount(expiredAmount)
                .message("환불이 완료되었습니다.")
                .build();
    }

}
