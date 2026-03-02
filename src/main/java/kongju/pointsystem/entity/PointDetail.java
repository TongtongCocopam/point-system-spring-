package kongju.pointsystem.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
public class PointDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private Timestamp expiredAt;
    @Column(nullable = false)
    private long amount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
