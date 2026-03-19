package kongju.pointsystem.domain.user.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kongju.pointsystem.global.error.exception.BalanceNotEnoughException;
import kongju.pointsystem.support.UserFixture;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UserBalanceTest {
    @Test
    @DisplayName("use 사용 시 총 잔액이 부족한 경우")
    void should_throw_exception_when_use_point() {
        User user = UserFixture.createWithBalance(500L);
        Assertions.assertThrows(BalanceNotEnoughException.class, () -> {
            user.getUserBalance().use(1000L);
        });
    }

    @Test
    @DisplayName("use 사용 시 잔액이 충분한 경우")
    void should_add_point_when_use_point() {
        User user = UserFixture.createWithBalance(500L);
        user.getUserBalance().use(300L);

        assertThat(user.getUserBalance().getTotalAmount()).isEqualTo(200L);
    }

    @Test
    @DisplayName("포인트 적립 성공")
    void should_earn_success_when_point_earn(){
        User user = UserFixture.create();

        user.getUserBalance().earn(1000L);
        assertThat(user.getUserBalance().getTotalAmount()).isEqualTo(1000L);
    }


    @Test
    @DisplayName("포인트 환불")
    void should_refund_success_when_point_refund(){
        User user = UserFixture.create();

        user.getUserBalance().refund(1000L);
        assertThat(user.getUserBalance().getTotalAmount()).isEqualTo(1000L);
    }
}
