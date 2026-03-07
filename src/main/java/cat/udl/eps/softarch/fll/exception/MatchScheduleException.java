package cat.udl.eps.softarch.fll.exception;

public class MatchScheduleException extends RuntimeException {
	
	private final MatchScheduleErrorCode errorCode;

	public MatchScheduleException(MatchScheduleErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public MatchScheduleErrorCode getErrorCode() {
		return errorCode;
	}
}