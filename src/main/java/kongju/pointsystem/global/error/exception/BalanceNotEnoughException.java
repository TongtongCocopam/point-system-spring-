package kongju.pointsystem.global.error.exception;

public class BalanceNotEnoughException extends BusinessException{
    public BalanceNotEnoughException() {
        super(ErrorCode.BALANCE_NOT_ENOUGH);
    }
}
