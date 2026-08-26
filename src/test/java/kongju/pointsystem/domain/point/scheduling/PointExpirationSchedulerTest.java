package kongju.pointsystem.domain.point.scheduling;


import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import kongju.pointsystem.support.UserFixture;
import kongju.pointsystem.domain.user.entity.User;
import kongju.pointsystem.domain.point.entity.PointType;
import kongju.pointsystem.domain.point.entity.PointUsage;
import kongju.pointsystem.domain.user.entity.UserBalance;
import kongju.pointsystem.domain.point.entity.PointDetail;
import kongju.pointsystem.domain.point.entity.PointHistory;
import kongju.pointsystem.domain.user.repository.UserBalanceRepository;
import kongju.pointsystem.domain.point.repository.PointUsageRepository;
import kongju.pointsystem.global.error.exception.UserNotFoundException;
import kongju.pointsystem.domain.point.repository.PointDetailRepository;
import kongju.pointsystem.domain.point.service.PointExpirationProcessor;
import kongju.pointsystem.domain.point.repository.PointHistoryRepository;


@ExtendWith(MockitoExtension.class)
public class PointExpirationSchedulerTest {
    @Mock
    private PointUsageRepository pointUsageRepository;

    @Mock
    private UserBalanceRepository userBalanceRepository;

    @Mock
    private PointDetailRepository pointDetailRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @InjectMocks
    private PointExpirationProcessor processor;

    @Test
    @DisplayName("만료 포인트가 있으면 상세 포인트와 전체 잔액을 차감하고 만료 이력을 저장")
    void should_expire_points_when_expired_points_exist() {
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        User user = UserFixture.createWithBalance(2500L);
        UserBalance userBalance = user.getUserBalance();

        PointDetail detail1 = PointDetail.builder()
                .user(user)
                .amount(1000L)
                .build();

        PointDetail detail2 = PointDetail.builder()
                .user(user)
                .amount(1500L)
                .build();

        when(userBalanceRepository.findByUserIdWithLock(userId))
                .thenReturn(Optional.of(userBalance));

        when(pointDetailRepository.findRemainedDetailsExpired(userId, now))
                .thenReturn(List.of(detail1, detail2));

        processor.expireUserPoints(userId, now);

        // 실제 엔티티 상태 변경 확인
        assertThat(detail1.getRemainAmount()).isZero();
        assertThat(detail2.getRemainAmount()).isZero();
        assertThat(userBalance.getTotalAmount()).isZero();

        // History 저장 내용 확인
        ArgumentCaptor<List<PointHistory>> historyCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(pointHistoryRepository)
                .saveAll(historyCaptor.capture());

        List<PointHistory> histories = historyCaptor.getValue();

        assertThat(histories).hasSize(2);
        assertThat(histories)
                .allMatch(history -> history.getType() == PointType.EXPIRE);

        assertThat(histories)
                .extracting(PointHistory::getAmount)
                .containsExactly(1000L, 1500L);

        // Usage 저장 확인
        ArgumentCaptor<List<PointUsage>> usageCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(pointUsageRepository)
                .saveAll(usageCaptor.capture());

        List<PointUsage> usages = usageCaptor.getValue();

        assertThat(usages).hasSize(2);
        assertThat(usages)
                .extracting(PointUsage::getAmount)
                .containsExactly(1000L, 1500L);
    }


    @Test
    @DisplayName("잔여 포인트가 0인 경우 해당 포인트는 만료 처리하지 않음")
    void should_skip_when_remain_amount_is_zero() {
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        User user = UserFixture.createWithBalance(0L);
        UserBalance userBalance = user.getUserBalance();

        PointDetail pointDetail = PointDetail.builder()
                .user(user)
                .amount(1000L)
                .build();

        // 이미 전부 사용된 PointDetail
        pointDetail.use(1000L);

        assertThat(pointDetail.getRemainAmount()).isZero();

        when(userBalanceRepository.findByUserIdWithLock(userId))
                .thenReturn(Optional.of(userBalance));

        when(pointDetailRepository.findRemainedDetailsExpired(userId, now))
                .thenReturn(List.of(pointDetail));

        processor.expireUserPoints(userId, now);

        assertThat(userBalance.getTotalAmount()).isZero();

        // 호출되지 않았는지 검증
        verify(pointHistoryRepository, never()).saveAll(any());

        verify(pointUsageRepository, never()).saveAll(any());
    }


    @Test
    @DisplayName("만료 대상 포인트가 없으면 아무 작업도 하지 않음")
    void should_do_nothing_when_expired_points_do_not_exist() {
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        User user = UserFixture.createWithBalance(1000L);
        UserBalance userBalance = user.getUserBalance();

        when(userBalanceRepository.findByUserIdWithLock(userId))
                .thenReturn(Optional.of(userBalance));

        when(pointDetailRepository.findRemainedDetailsExpired(userId, now))
                .thenReturn(List.of());

        processor.expireUserPoints(userId, now);

        // 기존 잔액 그대로
        assertThat(userBalance.getTotalAmount()).isEqualTo(1000L);

        verify(pointHistoryRepository, never()).saveAll(any());

        verify(pointUsageRepository, never()).saveAll(any());
    }


    @Test
    @DisplayName("UserBalance가 존재하지 않으면 예외")
    void should_throw_when_user_balance_not_found() {
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        when(userBalanceRepository.findByUserIdWithLock(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> processor.expireUserPoints(userId, now)
        );

        verify(pointDetailRepository, never())
                .findRemainedDetailsExpired(any(), any());

        verify(pointHistoryRepository, never()).saveAll(any());

        verify(pointUsageRepository, never()).saveAll(any());
    }
}
