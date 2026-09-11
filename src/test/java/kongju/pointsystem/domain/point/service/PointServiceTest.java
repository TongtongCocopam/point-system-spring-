package kongju.pointsystem.domain.point.service;

import java.awt.*;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.time.LocalDate;
import java.util.concurrent.*;
import java.time.LocalDateTime;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import kongju.pointsystem.domain.point.dto.*;
import kongju.pointsystem.domain.point.entity.PointType;
import kongju.pointsystem.domain.point.entity.PointUsage;
import kongju.pointsystem.domain.user.entity.UserBalance;
import kongju.pointsystem.global.error.exception.*;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import kongju.pointsystem.support.UserFixture;
import kongju.pointsystem.domain.user.entity.User;
import kongju.pointsystem.domain.point.entity.PointDetail;
import kongju.pointsystem.domain.point.entity.PointHistory;
import kongju.pointsystem.domain.user.repository.UserRepository;
import kongju.pointsystem.domain.point.repository.PointUsageRepository;
import kongju.pointsystem.domain.user.repository.UserBalanceRepository;
import kongju.pointsystem.domain.point.repository.PointDetailRepository;
import kongju.pointsystem.domain.point.repository.PointHistoryRepository;


@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PointDetailRepository pointDetailRepository;
    @Mock
    private PointHistoryRepository pointHistoryRepository;
    @Mock
    private PointExpirationProcessor pointExpirationProcessor;
    @Mock
    private UserBalanceRepository userBalanceRepository;
    @Mock
    private PointUsageRepository pointUsageRepository;

    @InjectMocks
    private PointService pointService;

    @Test
    @DisplayName("earn : 포인트를 성공적으로 적립했을 경우")
    void should_earn_success_when_points_are_earned() {
        UUID userId = UUID.randomUUID();
        User user = UserFixture.create();

        UserBalance userBalance = user.getUserBalance();
        when(userBalanceRepository.findByUserIdWithLock(eq(userId)))
                .thenReturn(Optional.of(userBalance));

        PointRequest request = new PointRequest(userId, 1000L);

        PointEarnResponse response = pointService.earnPoint(request);

        // UserBalance 락 조회
        verify(userBalanceRepository).findByUserIdWithLock(userId);

        // 포인트 디테일 생성
        verify(pointDetailRepository).save(any(PointDetail.class));
        // 포인트 히스토리 생성
        verify(pointHistoryRepository).save(any(PointHistory.class));
        // 유저 잔액 변경
        assertThat(response.currentBalance()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("earn : 0포인트를 적립했을 경우")
    void should_zero_point_earn_when_points_are_not_earned() {
        UUID userId = UUID.randomUUID();

        PointRequest request = new PointRequest(userId, 0L);

        InvalidPointAmountException exception = assertThrows(InvalidPointAmountException.class, () -> {
            pointService.earnPoint(request);
        });

        // 에러 메시지 확인
        assertThat(exception.getMessage()).isEqualTo("잘못된 적립 금액입니다");
    }

    @Test
    @DisplayName("earn : 음수를 적립했을 경우")
    void should_negative_point_earn_when_points_are_not_earned() {
        UUID userId = UUID.randomUUID();

        PointRequest request = new PointRequest(userId, -500L);

        InvalidPointAmountException exception = assertThrows(InvalidPointAmountException.class, () -> {
            pointService.earnPoint(request);
        });

        // 에러 메시지 확인
        assertThat(exception.getMessage()).isEqualTo("잘못된 적립 금액입니다");
    }

    @Test
    @DisplayName("earn : UserBalance가 존재하지 않는 경우")
    void should_find_id_when_not_exist() {
        UUID userId = UUID.randomUUID();
        when(userBalanceRepository.findByUserIdWithLock(eq(userId)))
                .thenReturn(Optional.empty());

        PointRequest request = new PointRequest(userId, 500L);

        UserBalanceNotFoundException exception = assertThrows(UserBalanceNotFoundException.class, () -> {
            pointService.earnPoint(request);
        });

        assertThat(exception.getMessage()).isEqualTo("UserBalance를 찾을 수 없습니다");
    }


    @Test
    @DisplayName("balance : 포인트가 성공적으로 조회될 경우")
    void should_balance_success_when_points_Check() {
        UUID userId = UUID.randomUUID();
        User user = UserFixture.create();
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        user.getUserBalance().earn(1000L);

        PointBalanceRequest request = new PointBalanceRequest(userId, null);
        PointBalanceResponse response = (PointBalanceResponse) pointService.balancePoint(request);

        assertThat(user.getUserBalance().getTotalAmount()).isEqualTo(1000L);
        assertThat(response.balance()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("balance : 만료 예정 시간을 넣은 경우")
    void should_null_expiredAt_when_points_Check() {
        UUID userId = UUID.randomUUID();
        User user = UserFixture.createWithBalance(2500L);

        PointDetail detail1 = PointDetail.builder()
                .amount(1000L)
                .user(user)
                .build();
        PointDetail detail2 = PointDetail.builder()
                .amount(1500L)
                .user(user)
                .build();

        LocalDateTime future = LocalDate.now()
                .plusMonths(1)
                .plusDays(1)
                .atStartOfDay();

        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        when(pointDetailRepository.findExpiringPointsBetween(
                eq(userId),
                any(LocalDateTime.class)
                , eq(future)
        )).thenReturn(List.of(detail1, detail2));

        PointBalanceRequest request = new PointBalanceRequest(userId, future);

        PointBalanceExpireResponse response = (PointBalanceExpireResponse) pointService.balancePoint(request);

        assertThat(user.getUserBalance().getTotalAmount()).isEqualTo(2500L);
        // 조회된 만료 예정 포인트 합계
        assertThat(response.balance()).isEqualTo(2500L);

        // 조회 기준 시각
        assertThat(response.expiredAt()).isEqualTo(future);
    }

    @Test
    @DisplayName("balance : 포인트가 없는경우")
    void should_return_zero_point_when_points_not_exist() {
        UUID userId = UUID.randomUUID();
        User user = UserFixture.create();
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        PointBalanceRequest request = new PointBalanceRequest(userId, null);

        PointBalanceResponse response = (PointBalanceResponse) pointService.balancePoint(request);

        assertThat(user.getUserBalance().getTotalAmount()).isEqualTo(0L);
        assertThat(response.balance()).isEqualTo(0L);
    }

    @Test
    @DisplayName("balance : User가 존재하지 않는 경우")
    void should_throw_UserNotFoundException_when_balance_user_not_found() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        PointBalanceRequest request = new PointBalanceRequest(userId, null);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            pointService.balancePoint(request);
        });

        assertThat(exception.getMessage()).isEqualTo("존재하지 않는 계정입니다");
    }

    @Test
    @DisplayName("use : 잔액이 1원 더 많은 경우")
    void should_use_success_when_balance_enough() {
        UUID userId = UUID.randomUUID();
        User user = UserFixture.createWithBalance(2500L);

        PointDetail pointDetail1 = PointDetail.builder()
                .user(user)
                .amount(1000)
                .build();
        PointDetail pointDetail2 = PointDetail.builder()
                .user(user)
                .amount(1500)
                .build();

        UserBalance userBalance = user.getUserBalance();

        when(userBalanceRepository.findByUserIdWithLock(eq(userId)))
                .thenReturn(Optional.of(userBalance));

        when(pointDetailRepository.findRemainedDetailsNotExpired(eq(userId), any())).thenReturn(List.of(pointDetail1, pointDetail2));

        PointRequest request = new PointRequest(userId, 2499L);
        PointUseResponse response = pointService.usePoint(request);

        assertThat(user.getUserBalance().getTotalAmount()).isEqualTo(1L);
        assertThat(pointDetail1.getRemainAmount()).isEqualTo(0L);
        assertThat(pointDetail2.getRemainAmount()).isEqualTo(1L);
        assertThat(response.currentBalance()).isEqualTo(1L);
    }

    @Test
    @DisplayName("use : 잔액이 부족한 경우 ")
    void should_throw_Balance_Not_Enough_exception_when_balance_not_enough() {
        UUID userId = UUID.randomUUID();
        User user = UserFixture.createWithBalance(1000L);

        UserBalance userBalance = user.getUserBalance();

        when(userBalanceRepository.findByUserIdWithLock(eq(userId)))
                .thenReturn(Optional.of(userBalance));

        PointRequest request = new PointRequest(userId, 1001L);
        BalanceNotEnoughException exception = assertThrows(BalanceNotEnoughException.class, () -> {
            pointService.usePoint(request);
        });

        assertThat(exception.getMessage()).isEqualTo("포인트 잔액이 부족합니다");
    }

    @Test
    @DisplayName("use : 잔액과 사용 금액이 일치하는 경우")
    void should_sucess_use_point_when_balance_enough() {
        UUID userId = UUID.randomUUID();
        User user = UserFixture.createWithBalance(2500L);

        UserBalance userBalance = user.getUserBalance();

        when(userBalanceRepository.findByUserIdWithLock(eq(userId)))
                .thenReturn(Optional.of(userBalance));

        PointDetail pointDetail1 = PointDetail.builder()
                .user(user)
                .amount(1000)
                .build();
        PointDetail pointDetail2 = PointDetail.builder()
                .user(user)
                .amount(1500)
                .build();

        when(pointDetailRepository.findRemainedDetailsNotExpired(eq(userId), any()))
                .thenReturn(List.of(pointDetail1, pointDetail2));

        PointRequest request = new PointRequest(userId, 2500L);
        PointUseResponse response = pointService.usePoint(request);

        assertThat(user.getUserBalance().getTotalAmount()).isEqualTo(0L);
        assertThat(pointDetail1.getRemainAmount()).isEqualTo(0L);
        assertThat(pointDetail2.getRemainAmount()).isEqualTo(0L);
        assertThat(response.currentBalance()).isEqualTo(0L);
    }

    @Test
    @DisplayName("use : 음수 잔액 차감")
    void should_not_valid_point_when_point_negative() {
        UUID userId = UUID.randomUUID();

        PointRequest request = new PointRequest(userId, -10L);

        InvalidPointAmountException exception = assertThrows(InvalidPointAmountException.class, () -> {
            pointService.usePoint(request);
        });

        assertThat(exception.getMessage()).isEqualTo("잘못된 적립 금액입니다");
    }

    @Test
    @DisplayName("use : UserBalance가 존재하지 않는 경우")
    void should_throw_User_Not_Enough_exception_when_point_use_user_not_found() {
        UUID userId = UUID.randomUUID();

        when(userBalanceRepository.findByUserIdWithLock(eq(userId)))
                .thenReturn(Optional.empty());
        PointRequest request = new PointRequest(userId, 100L);

        UserBalanceNotFoundException exception = assertThrows(UserBalanceNotFoundException.class, () -> {
            pointService.usePoint(request);
        });

        assertThat(exception.getMessage()).isEqualTo("UserBalance를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("refund : 환불 성공 케이스")
    void should_success_refund_point_when_point_use_user_found() {
        UUID userId = UUID.randomUUID();
        User user = UserFixture.create();
        UserBalance userBalance = user.getUserBalance();

        when(userBalanceRepository.findByUserIdWithLock(eq(userId))).thenReturn(Optional.of(userBalance));

        UUID referenceId = UUID.randomUUID();
        PointDetail pointDetail1 = PointDetail.builder()
                .amount(700L)
                .user(user)
                .build();

        PointDetail pointDetail2 = PointDetail.builder()
                .amount(300L)
                .user(user)
                .build();

        PointHistory useHistory = PointHistory.builder()
                .referenceId(referenceId)
                .type(PointType.USE)
                .user(user)
                .amount(1000L)
                .referenceId(referenceId)
                .build();

        PointUsage pointUsage1 = PointUsage.builder()
                .pointHistory(useHistory)
                .pointDetail(pointDetail1)
                .amount(700L)
                .build();
        PointUsage pointUsage2 = PointUsage.builder()
                .pointHistory(useHistory)
                .pointDetail(pointDetail2)
                .amount(300L)
                .build();

        useHistory.getPointUsages().add(pointUsage1);
        useHistory.getPointUsages().add(pointUsage2);

        when(pointHistoryRepository.existsReferenceId(userId, referenceId)).thenReturn(true);
        when(pointHistoryRepository.findRefundablePoints(userId, referenceId)).thenReturn(useHistory);

        RefundRequest request = new RefundRequest(userId, referenceId);
        PointRefundResponse response = pointService.refundPoint(request);

        assertThat(response.currentBalance()).isEqualTo(1000L);
        assertThat(user.getUserBalance().getTotalAmount()).isEqualTo(1000L);

    }

    @Test
    @DisplayName("환불 금액이 만료되어 아예 없는경우")
    void should_refund_failure_when_all_points_expired() {
        UUID userId = UUID.randomUUID();
        User user = UserFixture.create();
        UserBalance userBalance = user.getUserBalance();

        when(userBalanceRepository.findByUserIdWithLock(eq(userId))).thenReturn(Optional.of(userBalance));

        UUID referenceId = UUID.randomUUID();
        PointDetail pointDetail1 = PointDetail.builder()
                .amount(700L)
                .user(user)
                .build();

        PointDetail pointDetail2 = PointDetail.builder()
                .amount(300L)
                .user(user)
                .build();

        ReflectionTestUtils.setField(pointDetail1, "expiredAt", LocalDateTime.now().minusDays(10));
        ReflectionTestUtils.setField(pointDetail2, "expiredAt", LocalDateTime.now().minusDays(10));

        PointHistory useHistory = PointHistory.builder()
                .referenceId(referenceId)
                .type(PointType.USE)
                .user(user)
                .amount(1000L)
                .referenceId(referenceId)
                .build();

        PointUsage pointUsage1 = PointUsage.builder()
                .pointHistory(useHistory)
                .pointDetail(pointDetail1)
                .amount(700L)
                .build();
        PointUsage pointUsage2 = PointUsage.builder()
                .pointHistory(useHistory)
                .pointDetail(pointDetail2)
                .amount(300L)
                .build();

        useHistory.getPointUsages().add(pointUsage1);
        useHistory.getPointUsages().add(pointUsage2);

        when(pointHistoryRepository.existsReferenceId(userId, referenceId)).thenReturn(true);
        when(pointHistoryRepository.findRefundablePoints(userId, referenceId)).thenReturn(useHistory);

        RefundRequest request = new RefundRequest(userId, referenceId);
        PointRefundResponse response = pointService.refundPoint(request);

        assertThat(response.currentBalance()).isEqualTo(0L);
        assertThat(user.getUserBalance().getTotalAmount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("환불 금액과 만료된 금액이 있는경우")
    void should_refund_success_when_some_points_expired() {
        UUID userId = UUID.randomUUID();
        User user = UserFixture.create();
        UserBalance userBalance = user.getUserBalance();

        when(userBalanceRepository.findByUserIdWithLock(eq(userId))).thenReturn(Optional.of(userBalance));

        UUID referenceId = UUID.randomUUID();
        PointDetail pointDetail1 = PointDetail.builder()
                .amount(700L)
                .user(user)
                .build();

        PointDetail pointDetail2 = PointDetail.builder()
                .amount(300L)
                .user(user)
                .build();

        ReflectionTestUtils.setField(pointDetail1, "expiredAt", LocalDateTime.now().minusDays(10));

        PointHistory useHistory = PointHistory.builder()
                .referenceId(referenceId)
                .type(PointType.USE)
                .user(user)
                .amount(1000L)
                .referenceId(referenceId)
                .build();

        PointUsage pointUsage1 = PointUsage.builder()
                .pointHistory(useHistory)
                .pointDetail(pointDetail1)
                .amount(700L)
                .build();
        PointUsage pointUsage2 = PointUsage.builder()
                .pointHistory(useHistory)
                .pointDetail(pointDetail2)
                .amount(300L)
                .build();

        useHistory.getPointUsages().add(pointUsage1);
        useHistory.getPointUsages().add(pointUsage2);

        when(pointHistoryRepository.existsReferenceId(userId, referenceId)).thenReturn(true);
        when(pointHistoryRepository.findRefundablePoints(userId, referenceId)).thenReturn(useHistory);

        RefundRequest request = new RefundRequest(userId, referenceId);
        PointRefundResponse response = pointService.refundPoint(request);

        assertThat(response.currentBalance()).isEqualTo(300L);
        assertThat(user.getUserBalance().getTotalAmount()).isEqualTo(300L);
    }

    @Test
    @DisplayName("UserBalance가 존재하지 않는 경우")
    void should_refund_fail_when_user_not_found() {
        UUID userId = UUID.randomUUID();
        when(userBalanceRepository.findByUserIdWithLock(any())).thenReturn(Optional.empty());

        UUID referenceId = UUID.randomUUID();
        RefundRequest request = new RefundRequest(userId, referenceId);

        UserBalanceNotFoundException exception = assertThrows(UserBalanceNotFoundException.class, () -> {
            pointService.refundPoint(request);
        });

        assertThat(exception.getMessage()).isEqualTo("UserBalance를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("referenceId가 유효하지 않은 경우")
    void should_refund_fail_when_reference_id_invalid() {
        UUID userId = UUID.randomUUID();
        User user = UserFixture.create();
        UserBalance userBalance = user.getUserBalance();

        when(userBalanceRepository.findByUserIdWithLock(eq(userId))).thenReturn(Optional.of(userBalance));

        UUID referenceId = UUID.randomUUID();
        when(pointHistoryRepository.existsReferenceId(userId, referenceId)).thenReturn(false);

        RefundRequest request = new RefundRequest(userId, referenceId);

        RefundReferenceIdNotExsistException exception = assertThrows(RefundReferenceIdNotExsistException.class, () -> {
            pointService.refundPoint(request);
        });

        assertThat(exception.getMessage()).isEqualTo("존재하지 않는 영수증 번호 입니다");
    }


    @Test
    @DisplayName("환불이 이미 처리된 경우")
    void should_refund_fail_when_already_processed() {
        UUID userId = UUID.randomUUID();
        User user = UserFixture.create();
        UserBalance userBalance = user.getUserBalance();

        when(userBalanceRepository.findByUserIdWithLock(eq(userId))).thenReturn(Optional.of(userBalance));

        UUID referenceId = UUID.randomUUID();
        when(pointHistoryRepository.existsReferenceId(userId, referenceId)).thenReturn(true);
        when(pointHistoryRepository.findRefundPoints(userId, referenceId)).thenReturn(true);

        RefundRequest request = new RefundRequest(userId, referenceId);

        RefundAlreadyProcessedException exception = assertThrows(RefundAlreadyProcessedException.class, () -> {
            pointService.refundPoint(request);
        });

        assertThat(exception.getMessage()).isEqualTo("이미 환불 처리되었습니다");
    }

    @Test
    @DisplayName("만료된 포인트가 있으면 detail과 사용자 잔액을 차감하고 만료 이력을 저장하는 경우")
    void expirePoint_success() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        when(pointDetailRepository.findUsersWithExpiredPoints(any())).thenReturn(List.of(user1, user2));

        pointService.expirePoint();

        verify(pointExpirationProcessor).expireUserPoints(eq(user1), any());
    }

}
