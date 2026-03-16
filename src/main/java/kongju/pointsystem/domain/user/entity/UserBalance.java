package kongju.pointsystem.domain.user.entity;

import jakarta.persistence.*;
import kongju.pointsystem.global.error.exception.BalanceNotEnoughException;
import lombok.*;

import kongju.pointsystem.global.error.exception.PointInvalidException;

import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "user_balances")
public class UserBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private long totalAmount;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    public void earn(Long point) {
        // 유효한 포인트인지 확인
        if (point <= 0) {
            throw new PointInvalidException();
        }
        this.totalAmount += point;
    }

    public void use(Long point) {
        // 유효한 포인트인지 확인
        if (point <= 0) {
            throw new PointInvalidException();
        }

        // 총 금액 차감
        if (totalAmount < point) {
            throw new BalanceNotEnoughException();
        }

        this.totalAmount -= point;
    }

    public void refund(Long refundAmount) {
        this.totalAmount += refundAmount;
    }
}
