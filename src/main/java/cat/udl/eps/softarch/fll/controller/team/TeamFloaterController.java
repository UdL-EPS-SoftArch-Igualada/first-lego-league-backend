package cat.udl.eps.softarch.fll.controller.team;

import cat.udl.eps.softarch.fll.controller.dto.ApiErrorResponse;
import cat.udl.eps.softarch.fll.dto.AssignFloaterRequest;
import cat.udl.eps.softarch.fll.dto.AssignFloaterResponse;
import cat.udl.eps.softarch.fll.exception.TeamFloaterAssignmentException;
import cat.udl.eps.softarch.fll.service.floater.FloaterAssignmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teams")
public class TeamFloaterController {

	private final FloaterAssignmentService floaterAssignmentService;

	public TeamFloaterController(FloaterAssignmentService floaterAssignmentService) {
		this.floaterAssignmentService = floaterAssignmentService;
	}

	@PostMapping("/assign-floater")
	public AssignFloaterResponse assignFloater(@Valid @RequestBody AssignFloaterRequest request) {
		return floaterAssignmentService.assignFloater(request.getTeamId(), request.getFloaterId());
	}

	@ExceptionHandler(TeamFloaterAssignmentException.class)
	public ResponseEntity<ApiErrorResponse> handleAssignmentException(
		TeamFloaterAssignmentException ex,
		HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage(), request.getRequestURI()));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidRequestBody(
		HttpMessageNotReadableException exception,
		HttpServletRequest request) {
		return ResponseEntity.badRequest()
			.body(ApiErrorResponse.of("INVALID_ASSIGN_FLOATER_REQUEST", "Invalid request body", request.getRequestURI()));
	}
}
