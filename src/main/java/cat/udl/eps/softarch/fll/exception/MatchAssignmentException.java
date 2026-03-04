package cat.udl.eps.softarch.fll.exception;

public class MatchAssignmentException extends RuntimeException {
	private final MatchAssignmentErrorCode errorCode;
	private final Integer index;
	private final String matchId;
	private final String refereeId;

	public MatchAssignmentException(MatchAssignmentErrorCode errorCode, String message) {
		this(errorCode, message, null, null, null);
	}

	public MatchAssignmentException(
			MatchAssignmentErrorCode errorCode,
			String message,
			Integer index,
			String matchId,
			String refereeId) {
		super(message);
		this.errorCode = errorCode;
		this.index = index;
		this.matchId = matchId;
		this.refereeId = refereeId;
	}

	public MatchAssignmentErrorCode getErrorCode() {
		return errorCode;
	}

	public Integer getIndex() {
		return index;
	}

	public String getMatchId() {
		return matchId;
	}

	public String getRefereeId() {
		return refereeId;
	}

	public boolean hasBatchDetails() {
		return index != null;
	}
}
