package cat.udl.eps.softarch.fll.controller.team;

import cat.udl.eps.softarch.fll.dto.AssignCoachRequest;
import cat.udl.eps.softarch.fll.dto.AssignCoachResponse;
import cat.udl.eps.softarch.fll.controller.dto.ApiErrorResponse;
import cat.udl.eps.softarch.fll.exception.TeamCoachAssignmentException;
import cat.udl.eps.softarch.fll.service.coach.CoachService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamCoachController {

	private final CoachService teamCoachService;

	@PostMapping("/assign-coach")
	public AssignCoachResponse assignCoach(@Valid @RequestBody AssignCoachRequest request) {
		return teamCoachService.assignCoach(
			request.getTeamId(),
			request.getCoachId()
		);
	}

	@ExceptionHandler(TeamCoachAssignmentException.class)
	public ResponseEntity<ApiErrorResponse> handleAssignmentException(
		TeamCoachAssignmentException ex,
		HttpServletRequest request) {
		HttpStatus status = resolveStatus(ex.getErrorCode());
		return ResponseEntity.status(status)
			.body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage(), request.getRequestURI()));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidRequestBody(
		HttpMessageNotReadableException exception,
		HttpServletRequest request) {
		return ResponseEntity.badRequest()
			.body(ApiErrorResponse.of("INVALID_ASSIGN_COACH_REQUEST", "Invalid request body", request.getRequestURI()));
	}

	private HttpStatus resolveStatus(String errorCode) {
		return switch (errorCode) {
			case "TEAM_NOT_FOUND", "COACH_NOT_FOUND" -> HttpStatus.NOT_FOUND;
			case "COACH_ALREADY_ASSIGNED", "MAX_COACHES_PER_TEAM_REACHED", "MAX_TEAMS_PER_COACH_REACHED" ->
				HttpStatus.CONFLICT;
			default -> HttpStatus.BAD_REQUEST;
		};
	}
}
