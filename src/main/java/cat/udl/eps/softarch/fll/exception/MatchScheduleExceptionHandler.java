package cat.udl.eps.softarch.fll.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MatchScheduleExceptionHandler {

	@ExceptionHandler(MatchScheduleException.class)
	public ResponseEntity<DomainValidationErrorResponse> handleMatchScheduleException(MatchScheduleException ex) {
		
		HttpStatus status = switch (ex.getErrorCode()) {
			case INVALID_TIME_RANGE -> HttpStatus.UNPROCESSABLE_ENTITY;
			case TABLE_TIME_OVERLAP -> HttpStatus.CONFLICT;
		};

		DomainValidationErrorResponse response = new DomainValidationErrorResponse(
				ex.getErrorCode().name(),
				ex.getMessage()
		);

		return ResponseEntity.status(status).body(response);
	}
}