package cat.udl.eps.softarch.fll.controller.award;

import cat.udl.eps.softarch.fll.controller.dto.ApiErrorResponse;
import cat.udl.eps.softarch.fll.dto.AssignAwardRequest;
import cat.udl.eps.softarch.fll.dto.AssignAwardResponse;
import cat.udl.eps.softarch.fll.exception.AwardAssignmentException;
import cat.udl.eps.softarch.fll.service.award.AwardAssignmentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/awards")
public class AwardAssignmentController {

	private final AwardAssignmentService awardAssignmentService;

	public AwardAssignmentController(AwardAssignmentService awardAssignmentService) {
		this.awardAssignmentService = awardAssignmentService;
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/assign")
	public AssignAwardResponse assignAward(@RequestBody AssignAwardRequest request) {
		return awardAssignmentService.assignAward(request.awardId(), request.teamId());
	}

	@ExceptionHandler(AwardAssignmentException.class)
	public ResponseEntity<ApiErrorResponse> handleAssignmentException(
		AwardAssignmentException ex,
		HttpServletRequest request) {
		return ResponseEntity.status(resolveStatus(ex.getErrorCode()))
			.body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage(), request.getRequestURI()));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidRequestBody(HttpServletRequest request) {
		return ResponseEntity.badRequest()
			.body(ApiErrorResponse.of(
				"INVALID_AWARD_ASSIGNMENT_REQUEST",
				"Invalid request body",
				request.getRequestURI()));
	}

	private HttpStatus resolveStatus(String errorCode) {
		return switch (errorCode) {
			case "AWARD_NOT_FOUND", "TEAM_NOT_FOUND" -> HttpStatus.NOT_FOUND;
			case "AWARD_ALREADY_ASSIGNED" -> HttpStatus.CONFLICT;
			case "EDITION_MISMATCH" -> HttpStatus.UNPROCESSABLE_CONTENT;
			default -> HttpStatus.BAD_REQUEST;
		};
	}
}
