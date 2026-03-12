package kongju.pointsystem.domain.point.entity;

import jakarta.persistence.*;
import kongju.pointsystem.domain.user.entity.User;
import lombok.*;

import java.util.UUID;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PointUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private long amount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_history_id")
    private PointHistory pointHistory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_detail_id")
    private PointDetail pointDetail;
}
