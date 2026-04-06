package cat.udl.eps.softarch.fll.controller.match;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cat.udl.eps.softarch.fll.controller.match.dto.BatchMatchAssignmentRequest;
import cat.udl.eps.softarch.fll.controller.match.dto.BatchMatchAssignmentResponse;
import cat.udl.eps.softarch.fll.controller.match.dto.MatchAssignmentRequest;
import cat.udl.eps.softarch.fll.controller.match.dto.MatchAssignmentResponse;
import cat.udl.eps.softarch.fll.domain.Match;
import cat.udl.eps.softarch.fll.service.MatchAssignmentService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/matchAssignments")
public class MatchAssignmentController {
	private final MatchAssignmentService matchAssignmentService;

	public MatchAssignmentController(MatchAssignmentService matchAssignmentService) {
		this.matchAssignmentService = matchAssignmentService;
	}

	@PostMapping("/assign")
	public ResponseEntity<MatchAssignmentResponse> assignReferee(@Valid @RequestBody MatchAssignmentRequest request) {
		Match assignedMatch = matchAssignmentService.assignReferee(request.matchId(), request.refereeId());
		MatchAssignmentResponse response = new MatchAssignmentResponse(
			String.valueOf(assignedMatch.getId()),
			String.valueOf(assignedMatch.getReferee().getId()),
			"ASSIGNED");
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PostMapping("/batch")
	public ResponseEntity<BatchMatchAssignmentResponse> assignRefereesBatch(
		@Valid @RequestBody BatchMatchAssignmentRequest request) {
		BatchMatchAssignmentResponse response = matchAssignmentService.assignBatch(request.roundId(), request.assignments());
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
