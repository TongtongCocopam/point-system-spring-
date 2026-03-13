package kongju.pointsystem.domain.point.entity;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import kongju.pointsystem.domain.user.entity.User;
import lombok.*;


@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "point_histories")
public class PointHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PointType type;
    @Column(nullable = false)
    private long amount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false)
    private UUID referenceId;
    @Builder.Default
    @OneToMany(mappedBy = "pointHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PointUsage> pointUsages = new ArrayList<>();
}