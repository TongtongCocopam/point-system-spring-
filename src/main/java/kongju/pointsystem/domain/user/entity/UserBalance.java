package kongju.pointsystem.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import kongju.pointsystem.global.error.exception.BalanceNotEnoughException;


import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_balances")
public class UserBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private long totalAmount;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Builder
    public UserBalance(long totalAmount, User user) {
        this.totalAmount = totalAmount;
        this.user = user;
    }

    public void earn(Long point) {
        this.totalAmount += point;
    }

    public void use(Long point) {
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
