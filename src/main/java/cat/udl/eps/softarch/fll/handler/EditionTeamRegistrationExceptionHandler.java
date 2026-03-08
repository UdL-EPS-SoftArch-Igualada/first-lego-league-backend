package cat.udl.eps.softarch.fll.handler;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import cat.udl.eps.softarch.fll.exception.EditionTeamRegistrationException;

@RestControllerAdvice
public class EditionTeamRegistrationExceptionHandler {

	@ExceptionHandler(EditionTeamRegistrationException.class)
	public ResponseEntity<Map<String, String>> handleRegistrationException(EditionTeamRegistrationException e) {
		HttpStatus status = resolveStatus(e.getError());
		return ResponseEntity.status(status).body(Map.of(
				"error", e.getError(),
				"message", e.getMessage()));
	}

	private HttpStatus resolveStatus(String error) {
		return switch (error) {
			case "EDITION_NOT_FOUND", "TEAM_NOT_FOUND" -> HttpStatus.NOT_FOUND;
			case "MAX_TEAMS_REACHED", "TEAM_ALREADY_REGISTERED" -> HttpStatus.CONFLICT;
			default -> HttpStatus.BAD_REQUEST;
		};
	}
}




