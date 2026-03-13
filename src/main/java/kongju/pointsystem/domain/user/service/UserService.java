package kongju.pointsystem.domain.user.service;

import jakarta.validation.constraints.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import kongju.pointsystem.domain.user.dto.UserCreateResponse;
import kongju.pointsystem.domain.user.entity.User;
import kongju.pointsystem.domain.user.entity.UserBalance;
import kongju.pointsystem.domain.user.repository.UserBalanceRepository;
import kongju.pointsystem.domain.user.repository.UserRepository;
import kongju.pointsystem.global.error.exception.EmailDuplicatedException;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserBalanceRepository userBalanceRepository;

    public UserCreateResponse createUser(@Email String email, String name, String password) {
        //이메일 중복 확인
        if (userRepository.existsByEmail(email)) {
            throw new EmailDuplicatedException();
        }
        // 비밀번호 암호화 저장
        // 유저 생성
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .name(name)
                .build();
        userRepository.save(user);

        UserBalance userBalance = UserBalance.builder()
                .user(user)
                .totalAmount(0L)
                .build();
        userBalanceRepository.save(userBalance);

        return UserCreateResponse.builder()
                .email(email)
                .message("회원가입이 완료되었습니다")
                .build();
    }
}

