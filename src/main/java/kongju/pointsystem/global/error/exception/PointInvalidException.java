package kongju.pointsystem.global.error.exception;

public class PointInvalidException extends BusinessException{
    public PointInvalidException() {
        super(ErrorCode.POINT_INVALID);
    }
}
