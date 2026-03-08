package cat.udl.eps.softarch.fll.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import cat.udl.eps.softarch.fll.domain.Edition;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import cat.udl.eps.softarch.fll.domain.Team;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class DomainValidationExceptionHandler {

	@ExceptionHandler(DomainValidationException.class)
	public ResponseEntity<DomainValidationErrorResponse> handleDomainValidationException(DomainValidationException ex) {
		DomainValidationErrorResponse response = new DomainValidationErrorResponse(ex.getCode(), ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<DomainValidationErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
		if (isInvalidScientificProjectTeamReference(exception)) {
			DomainValidationErrorResponse response = new DomainValidationErrorResponse(
					"TEAM_NOT_FOUND", "The referenced team does not exist");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
		if (isInvalidScientificProjectEditionReference(exception)) {
			DomainValidationErrorResponse response = new DomainValidationErrorResponse(
					"EDITION_NOT_FOUND", "The referenced edition does not exist");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
		throw exception;
	}

	private boolean isInvalidScientificProjectTeamReference(HttpMessageNotReadableException exception) {
		return isInvalidReference(exception, Team.class, "team");
	}

	private boolean isInvalidScientificProjectEditionReference(HttpMessageNotReadableException exception) {
		return isInvalidReference(exception, Edition.class, "edition");
	}

	private boolean isInvalidReference(HttpMessageNotReadableException exception, Class<?> targetType, String fieldName) {
		if (!containsScientificProjectContext(exception)) {
			return false;
		}
		Throwable current = exception;
		while (current != null) {
			if (current instanceof InvalidFormatException invalidFormatException) {
				if (invalidFormatException.getTargetType() != null
						&& targetType.isAssignableFrom(invalidFormatException.getTargetType())) {
					return true;
				}
				boolean pathFound = invalidFormatException.getPath().stream()
						.anyMatch(ref -> fieldName.equals(ref.getFieldName()));
				if (pathFound) {
					return true;
				}
			}
			String message = current.getMessage();
			if (message != null && message.contains("ScientificProject") && message.contains(fieldName)) {
				return true;
			}
			current = current.getCause();
		}
		return false;
	}

	private boolean containsScientificProjectContext(Throwable throwable) {
		Throwable current = throwable;
		while (current != null) {
			String message = current.getMessage();
			if (message != null && message.contains("ScientificProject")) {
				return true;
			}
			current = current.getCause();
		}
		return false;
	}
}
