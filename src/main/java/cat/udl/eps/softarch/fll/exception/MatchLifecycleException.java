package cat.udl.eps.softarch.fll.exception;

import lombok.Getter;

@Getter
public class MatchLifecycleException extends RuntimeException {

	private final String error;

	public MatchLifecycleException(String error, String message) {
		super(message);
		this.error = error;
	}
}