package kongju.pointsystem.domain.point.entity;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

import kongju.pointsystem.domain.user.entity.User;
import kongju.pointsystem.support.UserFixture;


public class PointDetailTest {
    @Test
    @DisplayName("포인트 사용 시 잔액 정상 차감")
    void should_reduce_remainAmount_when_points_are_used() {
        User user = UserFixture.create();

        PointDetail pointDetail = PointDetail.builder()
                .amount(1000L)
                .user(user)
                .build();

        pointDetail.use(300L);
        assertThat(pointDetail.getRemainAmount()).isEqualTo(700L);
    }

    @Test
    @DisplayName("요청이 잔액보다 클 때 잔액 만큼 소진")
    void should_consume_remainAmount_when_request_exceeds_balance() {
        User user = UserFixture.create();

        PointDetail pointDetail = PointDetail.builder()
                .amount(800L)
                .user(user)
                .build();

        pointDetail.use(900L);
        assertThat(pointDetail.getRemainAmount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("생성 시 만료일이 1달 뒤로 설정됨")
    void should_set_expiredAt_to_one_month_later_on_creation() {
        User user = UserFixture.create();

        PointDetail pointDetail = PointDetail.builder()
                .amount(1000L)
                .user(user)
                .build();

        // 만료 시간
        LocalDateTime expectedTime = LocalDateTime.now().plusMonths(1);
        assertThat(pointDetail.getExpiredAt()).isAfterOrEqualTo(expectedTime.minusSeconds(5));
        assertThat(pointDetail.getExpiredAt()).isBeforeOrEqualTo(expectedTime.plusSeconds(5));
    }

    @Test
    @DisplayName("만료된 포인트 환불 시 0원 반환")
    void should_return_zero_refund_amount_when_point_is_expired() {
        User user = UserFixture.create();

        PointDetail pointDetail = PointDetail.builder()
                .amount(1000L)
                .user(user)
                .build();

        pointDetail.use(700L);

        LocalDateTime futureTime = LocalDateTime.now().plusMonths(2);
        Long refundResult = pointDetail.refund(500L, futureTime);
        // 환불 금액
        assertThat(refundResult).isEqualTo(0L);
        // 잔액
        assertThat(pointDetail.getRemainAmount()).isEqualTo(300L);
    }

    @Test
    @DisplayName("만료 전 환불 시 잔액 복구")
    void should_increase_remainAmount_when_refunded_before_expiry() {
        User user = UserFixture.create();

        PointDetail pointDetail = PointDetail.builder()
                .amount(1000L)
                .user(user)
                .build();

        pointDetail.use(700L);

        LocalDateTime futureTime = LocalDateTime.now();
        Long refundResult = pointDetail.refund(500L, futureTime);
        // 환불 금액
        assertThat(refundResult).isEqualTo(500L);
        // 잔액
        assertThat(pointDetail.getRemainAmount()).isEqualTo(800L);

    }
}
