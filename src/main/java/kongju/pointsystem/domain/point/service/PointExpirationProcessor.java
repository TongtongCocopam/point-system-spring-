package kongju.pointsystem.domain.point.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

import kongju.pointsystem.domain.user.entity.User;
import kongju.pointsystem.domain.point.entity.PointType;
import kongju.pointsystem.domain.point.entity.PointUsage;
import kongju.pointsystem.domain.user.entity.UserBalance;
import kongju.pointsystem.domain.point.entity.PointDetail;
import kongju.pointsystem.domain.point.entity.PointHistory;
import kongju.pointsystem.global.error.exception.UserNotFoundException;
import kongju.pointsystem.domain.user.repository.UserBalanceRepository;
import kongju.pointsystem.domain.point.repository.PointUsageRepository;
import kongju.pointsystem.domain.point.repository.PointDetailRepository;
import kongju.pointsystem.domain.point.repository.PointHistoryRepository;


@Service
@RequiredArgsConstructor
public class PointExpirationProcessor {
    private final PointUsageRepository pointUsageRepository;
    private final UserBalanceRepository userBalanceRepository;
    private final PointDetailRepository pointDetailRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Transactional
    public void expireUserPoints(UUID userId, LocalDateTime now) {
        UserBalance userBalance = userBalanceRepository.findByUserIdWithLock(userId)
                .orElseThrow(UserNotFoundException::new);

        User user = userBalance.getUser();

        List<PointDetail> pointDetails = pointDetailRepository.findRemainedDetailsExpired(
                userId,
                now
        );

        long totalExpire = 0L;

        List<PointHistory> histories = new ArrayList<>();
        List<PointUsage> usages = new ArrayList<>();

        for (PointDetail pointDetail : pointDetails) {
            long remainedAmount = pointDetail.getRemainAmount();

            if (remainedAmount <= 0) {
                continue;
            }

            pointDetail.use(remainedAmount);
            totalExpire += remainedAmount;

            PointHistory history = PointHistory.builder()
                    .user(user)
                    .type(PointType.EXPIRE)
                    .amount(remainedAmount)
                    .build();

            histories.add(history);

            usages.add(
                    PointUsage.builder()
                            .pointDetail(pointDetail)
                            .pointHistory(history)
                            .amount(remainedAmount)
                            .build()
            );
        }

        if (totalExpire == 0) {
            return;
        }

        userBalance.use(totalExpire);

        pointHistoryRepository.saveAll(histories);
        pointUsageRepository.saveAll(usages);
    }
}
