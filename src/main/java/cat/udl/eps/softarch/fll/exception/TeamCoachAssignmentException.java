package cat.udl.eps.softarch.fll.exception;

public class TeamCoachAssignmentException extends RuntimeException {

	private final String errorCode;

	public TeamCoachAssignmentException(String errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return errorCode;
	}
}
