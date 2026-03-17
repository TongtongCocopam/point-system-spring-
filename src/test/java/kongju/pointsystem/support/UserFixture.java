package kongju.pointsystem.support;

import kongju.pointsystem.domain.user.entity.User;
import kongju.pointsystem.domain.user.entity.UserBalance;

public class UserFixture {
    public static User create() {
        User user = User.builder()
                .email("test@gmail.com")
                .name("tester")
                .password("test1234")
                .build();

        UserBalance userBalance = UserBalance.builder()
                .user(user)
                .totalAmount(0L)
                .build();

        user.assignBalance(userBalance);
        return user;
    }
}
