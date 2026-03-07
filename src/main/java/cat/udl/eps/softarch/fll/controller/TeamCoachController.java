package cat.udl.eps.softarch.fll.controller;

import cat.udl.eps.softarch.fll.dto.AssignCoachRequest;
import cat.udl.eps.softarch.fll.dto.AssignCoachResponse;
import cat.udl.eps.softarch.fll.service.CoachService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamCoachController {

	private static final String ERROR_KEY = "error";

	private final CoachService teamCoachService;

	@PostMapping("/assign-coach")
	public AssignCoachResponse assignCoach(@Valid @RequestBody AssignCoachRequest request) {
		return teamCoachService.assignCoach(
			request.getTeamId(),
			request.getCoachId()
		);
	}

	@ExceptionHandler({NoSuchElementException.class, IllegalArgumentException.class})
	public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(Map.of(ERROR_KEY, ex.getMessage()));
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException ex) {
		String msg = ex.getMessage();
		if ("COACH_ALREADY_ASSIGNED".equals(msg)
			|| "MAX_COACHES_PER_TEAM_REACHED".equals(msg)
			|| "MAX_TEAMS_PER_COACH_REACHED".equals(msg)) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(Map.of(ERROR_KEY, msg));
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(Map.of(ERROR_KEY, msg));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handleUnexpected(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(Map.of(ERROR_KEY, "INTERNAL_SERVER_ERROR"));
	}
}