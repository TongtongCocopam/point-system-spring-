package kongju.pointsystem.domain.point.entity;

import java.time.LocalDateTime;
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

}
