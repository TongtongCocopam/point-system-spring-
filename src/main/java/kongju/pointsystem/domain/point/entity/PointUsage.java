package kongju.pointsystem.domain.point.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import kongju.pointsystem.domain.user.entity.User;

import java.util.UUID;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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

}
