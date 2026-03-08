package cat.udl.eps.softarch.fll.exception;

import lombok.Getter;

@Getter
public class EditionTeamRegistrationException extends RuntimeException {

	private final String error;

	public EditionTeamRegistrationException(String error, String message) {
		super(message);
		this.error = error;
	}
}

