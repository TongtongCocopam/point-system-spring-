package kongju.pointsystem.domain.point.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import kongju.pointsystem.domain.point.dto.*;
import kongju.pointsystem.global.error.exception.BalanceNotEnoughException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import kongju.pointsystem.domain.point.entity.PointDetail;
import kongju.pointsystem.domain.point.entity.PointHistory;
import kongju.pointsystem.domain.point.entity.PointType;
import kongju.pointsystem.domain.user.entity.User;
import kongju.pointsystem.domain.user.entity.UserBalance;
import kongju.pointsystem.domain.user.repository.UserRepository;
import kongju.pointsystem.global.error.exception.PointInvalidException;
import kongju.pointsystem.global.error.exception.UserNotFoundException;


import kongju.pointsystem.domain.point.repository.PointDetailRepository;
import kongju.pointsystem.domain.point.repository.PointHistoryRepository;
import kongju.pointsystem.domain.user.repository.UserBalanceRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class PointService {

    private final UserBalanceRepository balanceRepository;
    private final UserRepository userRepository;
    private final PointDetailRepository pointDetailRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final UserBalanceRepository userBalanceRepository;

    /**
     * 포인트 적립하는 서비스 로직
     *
     * @param userId 사용자 id
     * @param point  적립할 금액
     */
    @Transactional
    public PointEarnResponse earnPoint(UUID userId, Long point) {
        // id로 유저 확인
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        // 유효한 포인트인지 확인
        if (point <= 0) {
            throw new PointInvalidException();
        }
        // 유저 발란스가 있는지 확인 or 없으면 생성
        UserBalance balance = balanceRepository.findByUserIdWithLock(userId)
                .orElseGet(() -> UserBalance.builder()
                        .user(user)
                        .balance(0L)
                        .build());
        balanceRepository.save(balance);

        // 포인트 디테일 생성
        PointDetail pointdetail = PointDetail.builder()
                .expiredAt(LocalDateTime.now().plusMonths(1))
                .amount(point)
                .remainAmount(point)
                .user(user)
                .build();
        pointDetailRepository.save(pointdetail);

        // 포인트 히스토리 생성
        PointHistory pointHistory = PointHistory.builder()
                .type(PointType.EARN)
                .amount(point)
                .user(user)
                .build();
        pointHistoryRepository.save(pointHistory);
        return PointEarnResponse.builder()
                .earnedAmount(point)
                .currentBalance(balance.getBalance())
                .message("포인트가 적립되었습니다.")
                .build();

    }

    /**
     * 포인트 조회
     *
     * @param userId 사용자 id
     * @param time   만료 일자
     */
    @Transactional(readOnly = true)
    public PointResponse balancePoint(UUID userId, LocalDateTime time) {
        // id로 유저 확인
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        // 유저 발란스가 있는지 확인 or 없으면 생성
        UserBalance balance = balanceRepository.findByUserIdWithLock(userId)
                .orElseGet(() -> UserBalance.builder()
                        .user(user)
                        .balance(0L)
                        .build());

        if (time == null) {
            return PointBalanceResponse.builder()
                    .balance(balance.getBalance())
                    .build();
        }

        List<PointDetail> pointDetailList = pointDetailRepository.findByPointExpire(userId, time);
        Long totalAmount = pointDetailList
                .stream()
                .mapToLong(PointDetail::getRemainAmount)
                .sum();

        return PointBalanceExpireResponse.builder()
                .balance(totalAmount)
                .expiredAt(time)
                .build();
    }

    /**
     * 포인트 사용
     *
     * @param userId 사용자 id
     * @param point  사용할 포인트
     * @return
     */
    @Transactional
    public PointUseResponse usePoint(UUID userId, Long point) {
        if(point <= 0){
            throw new PointInvalidException();
        }

        //id로 유저 확인
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        // total_amount보다 작거나 같은지 확인
        UserBalance userBalance = userBalanceRepository.findByUserIdWithLock(userId)
                .orElseGet(() -> UserBalance.builder()
                        .user(user)
                        .balance(0L)
                        .build());

        if(point > userBalance.getBalance()){
            throw new BalanceNotEnoughException();
        }

        //유저 발란스 금액 차감
        userBalance.setBalance(userBalance.getBalance() - point);

        // 포인트 차감
        List<PointDetail> pointDetailList = pointDetailRepository
                .findByPointIdNow(userId, LocalDateTime.now());

        UUID referenceId = UUID.randomUUID();


        //포인트 리스트를 불러오기
        for(PointDetail pointDetail : pointDetailList){
            Long amount = pointDetail.getRemainAmount();
            // 포인트 차감
            if (point > amount) {
                pointDetail.setRemainAmount(0);
                point -= amount;

            }else{
                pointDetail.setRemainAmount(amount - point);

                break;
            }
        }
        // 포인트 히스토리 등록
        PointHistory pointHistory = PointHistory.builder()
                .amount(point)
                .type(PointType.USE)
                .user(user)
                .referenceId(referenceId)
                .pointDetail(pointDetailList)
                .build();
        // 출력
    }


}
