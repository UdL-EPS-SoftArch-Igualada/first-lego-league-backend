package cat.udl.eps.softarch.fll.exception;

public class RoomAssignmentException extends RuntimeException {

	private final String error;

	public RoomAssignmentException(String error, String message) {
		super(message);
		this.error = error;
	}

	public String getError() {
		return error;
	}
}
