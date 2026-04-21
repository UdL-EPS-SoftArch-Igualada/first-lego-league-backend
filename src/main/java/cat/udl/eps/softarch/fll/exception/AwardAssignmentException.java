package cat.udl.eps.softarch.fll.exception;

public class AwardAssignmentException extends RuntimeException {

	private final String errorCode;

	public AwardAssignmentException(String errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return errorCode;
	}
}
