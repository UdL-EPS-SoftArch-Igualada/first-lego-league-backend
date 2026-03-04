package cat.udl.eps.softarch.fll.exception;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class MatchAssignmentExceptionHandler {

	@ExceptionHandler(MatchAssignmentException.class)
	public ResponseEntity<?> handleMatchAssignmentException(
			MatchAssignmentException ex,
			HttpServletRequest request) {
		HttpStatus status = switch (ex.getErrorCode()) {
			case ROUND_NOT_FOUND, MATCH_NOT_FOUND, REFEREE_NOT_FOUND -> HttpStatus.NOT_FOUND;
			case AVAILABILITY_CONFLICT, MATCH_ALREADY_HAS_REFEREE -> HttpStatus.CONFLICT;
			case INVALID_ROLE, INVALID_MATCH_STATE, INVALID_ID_FORMAT -> HttpStatus.UNPROCESSABLE_CONTENT;
		};

		if (ex.hasBatchDetails()) {
			BatchErrorDetails details = new BatchErrorDetails(
					ex.getIndex(),
					ex.getMatchId(),
					ex.getRefereeId(),
					ex.getErrorCode().name());
			BatchErrorResponse body = new BatchErrorResponse(
					"BATCH_ASSIGNMENT_FAILED",
					"Assignment failed at index " + ex.getIndex(),
					Instant.now().toString(),
					request.getRequestURI(),
					details);
			return ResponseEntity.status(status).body(body);
		}

		return ResponseEntity.status(status).body(new ErrorResponse(ex.getErrorCode().name(), ex.getMessage()));
	}

	public record ErrorResponse(String error, String message) {}

	public record BatchErrorDetails(Integer index, String matchId, String refereeId, String cause) {}

	public record BatchErrorResponse(
			String error,
			String message,
			String timestamp,
			String path,
			BatchErrorDetails details) {}
}
