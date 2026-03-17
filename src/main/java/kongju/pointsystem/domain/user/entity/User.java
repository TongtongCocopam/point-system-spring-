package kongju.pointsystem.domain.user.entity;

import jakarta.persistence.*;
import kongju.pointsystem.domain.user.dto.UserCreateRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(unique = true, nullable = false, length = 25)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String name;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserBalance userBalance;

    @Builder
    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public void assignBalance(UserBalance userBalance) {
        this.userBalance = userBalance;
        if(userBalance.getUser() != this){
            userBalance.updateUser(this);
        }
    }

    public static User createUser(String email, String password, String name){
        User user = User.builder()
                .email(email)
                .password(password)
                .name(name)
                .build();

        UserBalance balance = UserBalance.builder()
                .totalAmount(0L)
                .user(user)
                .build();
        user.assignBalance(balance);

        return user;
    }
}
