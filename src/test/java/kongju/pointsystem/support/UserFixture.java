package kongju.pointsystem.support;

import kongju.pointsystem.domain.user.entity.User;

public class UserFixture {
    public static User create() {
        return User.builder()
                .email("test@gmail.com")
                .name("tester")
                .password("test1234")
                .build();
    }
}
