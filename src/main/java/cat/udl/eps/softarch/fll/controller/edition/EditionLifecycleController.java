package cat.udl.eps.softarch.fll.controller.edition;

import java.util.Arrays;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import cat.udl.eps.softarch.fll.controller.dto.ApiErrorResponse;
import cat.udl.eps.softarch.fll.domain.EditionState;
import cat.udl.eps.softarch.fll.exception.EditionLifecycleException;
import cat.udl.eps.softarch.fll.service.EditionLifecycleService;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Edition Lifecycle", description = "Custom endpoint to transition edition lifecycle state")
public class EditionLifecycleController {

	private final EditionLifecycleService editionLifecycleService;

	public EditionLifecycleController(EditionLifecycleService editionLifecycleService) {
		this.editionLifecycleService = editionLifecycleService;
	}

	@PreAuthorize("isAuthenticated()")
	@PatchMapping("/editions/{editionId}/state")
	@Operation(summary = "Transition edition state")
	public ChangeEditionStateResponse changeState(@PathVariable Long editionId, @RequestBody ChangeEditionStateRequest request) {
		if (request == null || request.state() == null) {
			throw new EditionLifecycleException("INVALID_EDITION_STATE_REQUEST", "State is required");
		}
		EditionLifecycleService.TransitionResult transition = editionLifecycleService.changeState(editionId, request.state());
		return new ChangeEditionStateResponse(
			transition.editionId(),
			transition.previousState(),
			transition.newState(),
			"UPDATED");
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleUnreadablePayload(
		HttpMessageNotReadableException exception,
		HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ApiErrorResponse.of(
				"INVALID_EDITION_STATE_REQUEST",
				buildInvalidPayloadMessage(exception),
				request.getRequestURI()));
	}

	private String buildInvalidPayloadMessage(HttpMessageNotReadableException exception) {
		Throwable cause = exception;
		while (cause != null) {
			if (isInvalidStateValueError(cause)) {
				return "Invalid state value. Allowed values: " + Arrays.toString(EditionState.values());
			}
			cause = cause.getCause();
		}

		cause = exception.getMostSpecificCause();
		if (cause instanceof MismatchedInputException mismatchedInputException
			&& mismatchedInputException.getMessage() != null
			&& mismatchedInputException.getMessage().contains("No content to map")) {
			return "Request body is required";
		}

		return "Invalid request body";
	}

	private boolean isInvalidStateValueError(Throwable cause) {
		if (cause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException invalidFormatException) {
			return invalidFormatException.getTargetType() != null
				&& EditionState.class.isAssignableFrom(invalidFormatException.getTargetType());
		}
		if (cause instanceof tools.jackson.databind.exc.InvalidFormatException invalidFormatException) {
			return invalidFormatException.getTargetType() != null
				&& EditionState.class.isAssignableFrom(invalidFormatException.getTargetType());
		}
		return false;
	}

	public record ChangeEditionStateRequest(EditionState state) {
	}

	public record ChangeEditionStateResponse(Long editionId, EditionState previousState, EditionState newState,
	                                         String status) {
	}
}
