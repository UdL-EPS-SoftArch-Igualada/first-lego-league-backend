package cat.udl.eps.softarch.fll.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.Map;
import java.util.HashMap;
import cat.udl.eps.softarch.fll.api.dto.AssignJudgeRequest;
import cat.udl.eps.softarch.fll.api.dto.AssignJudgeResponse;
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
	public ResponseEntity<Map<String, String>> handleRoomAssignmentException(RoomAssignmentException e) {
		Map<String, String> error = new HashMap<>();
		error.put("error", e.getError());
		error.put("message", e.getMessage());
		return ResponseEntity.badRequest().body(error);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handleUnexpectedException(Exception e) {
		Map<String, String> error = new HashMap<>();
		error.put("error", "SERVER_ERROR");
		error.put("message", "Internal server error");
		return ResponseEntity.internalServerError().body(error);
	}
}