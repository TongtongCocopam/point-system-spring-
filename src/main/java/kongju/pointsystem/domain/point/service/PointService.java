package kongju.pointsystem.domain.point.service;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.transaction.Transactional;
import kongju.pointsystem.domain.point.entity.PointDetail;
import kongju.pointsystem.domain.point.entity.PointHistory;
import kongju.pointsystem.domain.point.entity.PointType;
import kongju.pointsystem.domain.user.entity.User;
import kongju.pointsystem.domain.user.entity.UserBalance;
import kongju.pointsystem.domain.user.repository.UserRepository;
import kongju.pointsystem.global.error.exception.PointInvalidException;
import kongju.pointsystem.global.error.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import kongju.pointsystem.domain.point.dto.PointEarnResponse;
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

    /**
     * 포인트 적립하는 서비스 로직
     *
     * @param userId 사용자 id
     * @param point  적립할 금액
     */
    @Transactional
    public PointEarnResponse earnPoint(UUID userId, Long point) {
        // id로 유저 확인
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        // 유효한 포인트인지 확인
        if (point <= 0) {
            throw new PointInvalidException();
        }
        // 유저 발란스가 있는지 확인 or 없으면 생성
        UserBalance balance = balanceRepository.findByUserIdWithLock(userId)
                .orElseGet(() -> UserBalance.builder()
                        .user(user)
                        .balance(0L)
                        .build());
        balanceRepository.save(balance);

        // 포인트 디테일 생성
        PointDetail pointdetail = PointDetail.builder()
                .expiredAt(LocalDateTime.now().plusMonths(1))
                .amount(point)
                .user(user)
                .build();
        pointDetailRepository.save(pointdetail);

        UUID referenceId = UUID.randomUUID();
        // 포인트 히스토리 생성
        PointHistory pointHistory = PointHistory.builder()
                .type(PointType.EARN)
                .amount(point)
                .user(user)
                .referenceId(referenceId)
                .build();
        pointHistoryRepository.save(pointHistory);
        return PointEarnResponse.builder()
                .earnedAmount(point)
                .currentBalance(balance.getBalance())
                .message("포인트가 적립되었습니다.")
                .build();

    }
}
