package cat.udl.eps.softarch.fll.exception;

public class TeamFloaterAssignmentException extends RuntimeException {

	private final String errorCode;

	public TeamFloaterAssignmentException(String errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return errorCode;
	}
}
