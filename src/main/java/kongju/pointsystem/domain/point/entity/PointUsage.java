package kongju.pointsystem.domain.point.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "point_usage")
public class PointUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private Long amount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_detail_id", nullable = false)
    private PointDetail pointDetail;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_history_id", nullable = false)
    private PointHistory pointHistory;

    @Builder
    public PointUsage(Long amount, PointDetail pointDetail, PointHistory pointHistory) {
        this.amount = amount;
        this.pointDetail = pointDetail;
        this.pointHistory = pointHistory;
    }
}
