package cat.udl.eps.softarch.fll.controller.match;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cat.udl.eps.softarch.fll.controller.match.dto.MatchTableAssignmentRequest;
import cat.udl.eps.softarch.fll.controller.match.dto.MatchTableAssignmentResponse;
import cat.udl.eps.softarch.fll.service.MatchTableAssignmentService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/matches")
public class MatchController {

	private final MatchTableAssignmentService matchTableAssignmentService;

	public MatchController(MatchTableAssignmentService matchTableAssignmentService) {
		this.matchTableAssignmentService = matchTableAssignmentService;
	}

	@PostMapping("/{matchId}/assign-table")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<MatchTableAssignmentResponse> assignTable(
		@PathVariable Long matchId,
		@Valid @RequestBody MatchTableAssignmentRequest request) {
		MatchTableAssignmentResponse response = matchTableAssignmentService.assignTable(matchId, request.tableIdentifier());
		return ResponseEntity.ok(response);
	}
}
