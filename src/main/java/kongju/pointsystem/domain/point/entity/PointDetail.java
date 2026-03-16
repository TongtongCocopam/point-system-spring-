package kongju.pointsystem.domain.point.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import kongju.pointsystem.domain.user.entity.User;
import lombok.*;


@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "point_details")
public class PointDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private LocalDateTime expiredAt;
    @Column(nullable = false)
    private long amount;
    @Column(nullable = false)
    private long remainAmount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @OneToMany(mappedBy = "pointDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PointUsage> pointUsages = new ArrayList<>();

    public Long use(Long point) {
        if (this.remainAmount <= 0) {
            return 0L;
        }

        long consumedAmount = Math.min(remainAmount, point);
        this.remainAmount -= consumedAmount;

        return consumedAmount;
    }

    public Long refund(Long amount) {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime expiredAt = this.expiredAt;

        if (expiredAt.isAfter(now)) {
            this.remainAmount += amount;
            return amount;
        }
        return 0L;
    }

}
