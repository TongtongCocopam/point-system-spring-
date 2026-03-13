package kongju.pointsystem.domain.user.repository;

import java.util.UUID;

import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;

import kongju.pointsystem.domain.user.entity.User;


public interface UserRepository extends JpaRepository<User, UUID> {
    boolean findUserByEmail(@Email String email);
}
