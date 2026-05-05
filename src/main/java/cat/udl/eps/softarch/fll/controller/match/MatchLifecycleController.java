package cat.udl.eps.softarch.fll.controller.match;

import java.util.Arrays;
import cat.udl.eps.softarch.fll.controller.dto.ApiErrorResponse;
import cat.udl.eps.softarch.fll.domain.match.MatchState;
import cat.udl.eps.softarch.fll.exception.MatchLifecycleException;
import cat.udl.eps.softarch.fll.service.match.MatchLifecycleService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/matches")
public class MatchLifecycleController {

	private final MatchLifecycleService matchLifecycleService;

	public MatchLifecycleController(MatchLifecycleService matchLifecycleService) {
		this.matchLifecycleService = matchLifecycleService;
	}

	@PreAuthorize("hasRole('ADMIN') or hasRole('REFEREE')")
	@PatchMapping("/{matchId}/state")
	public ChangeMatchStateResponse changeState(
			@PathVariable Long matchId,
			@RequestBody ChangeMatchStateRequest request) {
		if (request == null || request.state() == null) {
			throw new MatchLifecycleException("INVALID_MATCH_STATE_REQUEST", "State is required");
		}
		MatchLifecycleService.TransitionResult transition =
			matchLifecycleService.changeState(matchId, request.state());
		return new ChangeMatchStateResponse(
			transition.matchId(),
			transition.previousState(),
			transition.newState(),
			"UPDATED");
	}

	@ExceptionHandler(MatchLifecycleException.class)
	public ResponseEntity<ApiErrorResponse> handleLifecycleException(
			MatchLifecycleException ex, HttpServletRequest request) {
		HttpStatus status = switch (ex.getError()) {
			case "MATCH_NOT_FOUND" -> HttpStatus.NOT_FOUND;
			case "INVALID_MATCH_STATE_TRANSITION" -> HttpStatus.BAD_REQUEST;
			default -> HttpStatus.BAD_REQUEST;
		};
		return ResponseEntity.status(status)
			.body(ApiErrorResponse.of(ex.getError(), ex.getMessage(), request.getRequestURI()));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleUnreadable(
			HttpMessageNotReadableException ex, HttpServletRequest request) {
		Throwable cause = ex.getCause();
		String message = "Invalid request body";
		while (cause != null) {
			if (cause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException ife
					&& ife.getTargetType() != null
					&& MatchState.class.isAssignableFrom(ife.getTargetType())) {
				message = "Invalid state value. Allowed values: " + Arrays.toString(MatchState.values());
				break;
			}
			cause = cause.getCause();
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ApiErrorResponse.of("INVALID_MATCH_STATE_REQUEST", message, request.getRequestURI()));
	}

	public record ChangeMatchStateRequest(MatchState state) {}

	public record ChangeMatchStateResponse(
		Long matchId, MatchState previousState, MatchState newState, String status) {}
}