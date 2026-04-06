package cat.udl.eps.softarch.fll.controller.project;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ExceptionHandler;
import cat.udl.eps.softarch.fll.api.dto.AssignJudgeRequest;
import cat.udl.eps.softarch.fll.api.dto.AssignJudgeResponse;
import cat.udl.eps.softarch.fll.controller.dto.ApiErrorResponse;
import cat.udl.eps.softarch.fll.exception.RoomAssignmentException;
import cat.udl.eps.softarch.fll.service.ProjectRoomAssignmentService;

@RestController
@RequestMapping("/project-rooms")
public class ProjectRoomController {

	private final ProjectRoomAssignmentService projectRoomAssignmentService;

	public ProjectRoomController(ProjectRoomAssignmentService projectRoomAssignmentService) {
		this.projectRoomAssignmentService = projectRoomAssignmentService;
	}

	@PostMapping("/assign-judge")
	public ResponseEntity<AssignJudgeResponse> assignJudge(@RequestBody AssignJudgeRequest request) {
		AssignJudgeResponse response = projectRoomAssignmentService.assignJudge(request);
		return ResponseEntity.ok(response);
	}

	@ExceptionHandler(RoomAssignmentException.class)
	public ResponseEntity<ApiErrorResponse> handleRoomAssignmentException(
		RoomAssignmentException e,
		HttpServletRequest request) {
		return ResponseEntity.status(resolveStatus(e.getError()))
			.body(ApiErrorResponse.of(e.getError(), e.getMessage(), request.getRequestURI()));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidRequestBody(HttpMessageNotReadableException exception,
	                                                                 HttpServletRequest request) {
		return ResponseEntity.badRequest()
			.body(ApiErrorResponse.of("INVALID_ASSIGN_JUDGE_REQUEST", "Invalid request body", request.getRequestURI()));
	}

	private org.springframework.http.HttpStatus resolveStatus(String error) {
		return switch (error) {
			case "INVALID_JUDGE_ID_FORMAT" -> org.springframework.http.HttpStatus.BAD_REQUEST;
			case "ROOM_NOT_FOUND", "JUDGE_NOT_FOUND" -> org.springframework.http.HttpStatus.NOT_FOUND;
			case "ROOM_ALREADY_HAS_MANAGER", "MAX_PANELISTS_REACHED", "JUDGE_ALREADY_ASSIGNED" ->
				org.springframework.http.HttpStatus.CONFLICT;
			default -> org.springframework.http.HttpStatus.BAD_REQUEST;
		};
	}
}
