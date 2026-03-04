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
		this.matchId = index != null ? defaultBatchValue(matchId) : null;
		this.refereeId = index != null ? defaultBatchValue(refereeId) : null;
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
		return index != null && matchId != null && refereeId != null;
	}

	private String defaultBatchValue(String value) {
		return value != null ? value : "UNKNOWN";
	}
}
