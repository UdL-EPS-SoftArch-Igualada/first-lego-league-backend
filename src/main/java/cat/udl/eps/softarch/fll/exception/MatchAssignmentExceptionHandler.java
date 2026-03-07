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
	public ResponseEntity<ErrorResponse> handleMatchAssignmentException(
			MatchAssignmentException ex,
			HttpServletRequest request) {
		HttpStatus status = switch (ex.getErrorCode()) {
			case ROUND_NOT_FOUND, MATCH_NOT_FOUND, REFEREE_NOT_FOUND -> HttpStatus.NOT_FOUND;
			case AVAILABILITY_CONFLICT, MATCH_ALREADY_HAS_REFEREE, DUPLICATE_MATCH_IN_BATCH -> HttpStatus.CONFLICT;
			case INVALID_ROLE, INVALID_MATCH_STATE, INVALID_ID_FORMAT -> HttpStatus.UNPROCESSABLE_CONTENT;
		};

		BatchErrorDetails details = ex.hasBatchDetails()
				? new BatchErrorDetails(ex.getIndex(), ex.getMatchId(), ex.getRefereeId(), ex.getErrorCode().name())
				: null;
		ErrorResponse body = new ErrorResponse(
				ex.hasBatchDetails() ? "BATCH_ASSIGNMENT_FAILED" : ex.getErrorCode().name(),
				ex.hasBatchDetails() ? "Assignment failed at index " + ex.getIndex() : ex.getMessage(),
				Instant.now().toString(),
				request.getRequestURI(),
				details);
		return ResponseEntity.status(status).body(body);
	}

	public record ErrorResponse(
			String error,
			String message,
			String timestamp,
			String path,
			BatchErrorDetails details) {}

	public record BatchErrorDetails(Integer index, String matchId, String refereeId, String cause) {}
}
