package kongju.pointsystem.domain.user.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UserTest {
    @Test
    @DisplayName("잔액 0원으로 잘 생성되고 잔액과 연결 되어있는지 확인")
    void should_success_create_when_create_user(){
        User user = User.createUser("tset1234@gmail.com", "test1234", "tester");

        assertThat(user.getEmail()).isEqualTo("tset1234@gmail.com");
        assertThat(user.getName()).isEqualTo("tester");
        assertThat(user.getUserBalance().getTotalAmount()).isEqualTo(0);
    }


}
