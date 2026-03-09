package kongju.pointsystem.domain.point.entity;

import jakarta.persistence.*;
import kongju.pointsystem.domain.user.entity.User;

import java.util.UUID;

@Entity
public class PointHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private String type;
    @Column(nullable = false)
    private long amount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false)
    private long referenceId;
}