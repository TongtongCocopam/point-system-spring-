package kongju.pointsystem.domain.user.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import kongju.pointsystem.domain.user.entity.User;


public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsById(UUID id);
}
