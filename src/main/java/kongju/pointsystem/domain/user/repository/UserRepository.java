package kongju.pointsystem.domain.user.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.validation.constraints.Email;
import kongju.pointsystem.domain.user.entity.User;
import kongju.pointsystem.global.error.exception.UserNotFoundException;


public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(@Email String email);

    default User findByIdOrThrow(UUID id){
        return findById(id).orElseThrow(UserNotFoundException::new);
    }
}
